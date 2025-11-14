package net.sf.l2j.gameserver.util.sellBuffEngine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.PrivateStoreType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopMsg;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.util.sellBuffEngine.BuffShopConfigs.Cost;
import net.sf.l2j.gameserver.util.sellBuffEngine.BuffShopConfigs.SkillPath;
import net.sf.l2j.gameserver.util.sellBuffEngine.ShopObject.PrivateBuff;
/*
L2jbrasil Dhousefe
*/

/**
 * Gerencia o ciclo de vida e a l�gica central das lojas de buffs. Esta classe atua como um servi�o, orquestrando a cria��o, remo��o e intera��o com as lojas.
 */
public final class BuffShopManager
{
	private static final Logger _log = Logger.getLogger(BuffShopManager.class.getName());
	private static final int OFFLINE_SHOPS_PER_TICK = 2; // Quantas lojas restaurar por vez.
	private static final int OFFLINE_SHOPS_TICK_DELAY = 2000; // Delay em milissegundos (2 segundos) entre cada lote.
	
	private final BuffShopDAO dao;
	private final BuffShopFactory factory;
	private final Map<Integer, ShopObject> shops = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> sellers = new ConcurrentHashMap<>();
	private final Map<Integer, ShopObject> playerProfiles = new ConcurrentHashMap<>();
	
	private static class SingletonHolder
	{
		private static final BuffShopManager _instance = new BuffShopManager();
	}
	
	public static BuffShopManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private BuffShopManager()
	{
		this.dao = new BuffShopDAO();
		this.factory = BuffShopFactory.getInstance();
		_log.info("BuffShopManager: Sistema de Lojas de Buffs inicializado.");
	}
	
	public void startShopSetup(final Player player)
	{
		if (!BuffShopConfigs.BUFFSHOP_ALLOW_CLASS.contains(player.getClassId().getId()))
		{
			player.sendMessage("Sua classe n�o tem permiss�o para criar uma loja de buffs.");
			return;
		}
		BuffShopUIManager.getInstance().showManagementWindow(player, 1);
	}
	
	public void addBuffToProfile(final Player player, final int skillId, final int skillLevel, final int price)
	{
		final ShopObject shopProfile = getProfile(player);
		if (shopProfile.getBuffList().size() >= BuffShopConfigs.BUFFSHOP_BUFFS_MAX_COUNT)
		{
			player.sendMessage("Voc� atingiu o limite m�ximo de buffs na loja.");
			return;
		}
		shopProfile.addBuff(skillId, skillLevel, price);
	}
	
	public void removeBuffFromProfile(final Player player, final int skillId)
	{
		getProfile(player).removeBuff(skillId);
	}
	
	public void startShop(final Player player)
	{
		final ShopObject shopConfig = getProfile(player);
		// Chama o m�todo de verifica��o do core primeiro
		if (!player.canOpenPrivateStore(true))
		{
			return; // Impede a abertura da loja se qualquer condi��o do jogo n�o for atendida
		}
		
		if (shopConfig.getBuffList().isEmpty())
		{
			player.sendMessage("Voc� precisa adicionar pelo menos um buff para vender.");
			return;
		}
		if (shops.containsKey(player.getObjectId()))
		{
			stopShop(player);
		}
		
		final Player sellerNpc = factory.createShopNpc(player, shopConfig);
		if (sellerNpc == null)
		{
			player.sendMessage("Ocorreu um erro ao criar sua loja.");
			return;
		}
		
		shopConfig.setSellerNpcObjectId(sellerNpc.getObjectId());
		shopConfig.setXYZ(player.getX(), player.getY(), player.getZ(), player.getHeading());
		this.shops.put(player.getObjectId(), shopConfig);
		this.sellers.put(sellerNpc.getObjectId(), player.getObjectId());
		
		sellerNpc.getManufactureList().setStoreName(shopConfig.getStoreMessage());
		sellerNpc.setPrivateStoreType(PrivateStoreType.MANUFACTURE);
		sellerNpc.spawnMe(player.getX(), player.getY(), player.getZ());
		World.getInstance().addPlayer(sellerNpc);
		sellerNpc.setOnlineStatus(true, false);
		sellerNpc.sitDown();
		sellerNpc.broadcastUserInfo();
		sellerNpc.broadcastPacket(new RecipeShopMsg(sellerNpc));
		
		ThreadPool.execute(() -> dao.saveShop(shopConfig));
		player.sendMessage("Sua loja de buffs foi aberta com sucesso.");
	}
	
	public void stopShop(final Player playerOwner)
	{
		final ShopObject activeShop = shops.remove(playerOwner.getObjectId());
		if (activeShop != null)
		{
			final int npcId = activeShop.getSellerNpcObjectId();
			if (npcId > 0)
			{
				sellers.remove(npcId);
				final Player sellerNpc = World.getInstance().getPlayer(npcId);
				if (sellerNpc != null)
				{
					sellerNpc.deleteMe();
				}
			}
			ThreadPool.execute(() -> dao.removeShop(playerOwner.getObjectId()));
		}
	}
	
	public void sellBuff(final Player sellerNpc, final Player buyer, final int buffId, final int buffLevel, final String targetType, final int page)
	{
		final Integer ownerId = sellers.get(sellerNpc.getObjectId());
		if (ownerId == null)
			return;
		final ShopObject shop = shops.get(ownerId);
		if (shop == null)
			return;
		
		// Passa o ID e o N�vel para a transa��o
		final BuffShopTransaction transaction = new BuffShopTransaction(buyer, sellerNpc, shop, buffId, buffLevel, targetType);
		
		if (transaction.execute())
		{
			final PrivateBuff soldBuff = shop.getBuff(buffId); // O pre�o ainda vem do buff original da loja
			if (soldBuff != null)
			{
				rewardSeller(shop, soldBuff.price());
			}
		}
		
		int currentTab = "pet".equalsIgnoreCase(targetType) ? 2 : 1;
		BuffShopUIManager.getInstance().showPublicShopWindow(buyer, sellerNpc, shop, currentTab, page);
	}
	
	/**
	 * [VERS�O OTIMIZADA] Dispara a restaura��o ass�ncrona das lojas offline para n�o atrasar a inicializa��o do servidor.
	 */
	public void restoreOfflineTraders()
	{
		_log.info("BuffShopManager: Agendando restaura��o de lojas de buffs offline...");
		
		// 1. DAO carrega a lista de ShopObjects de forma r�pida, pois � s� uma query.
		final List<ShopObject> offlineShops = dao.loadShops();
		
		if (offlineShops.isEmpty())
		{
			_log.info("BuffShopManager: Nenhuma loja de buffs offline para restaurar.");
			return;
		}
		
		// 2. Cria uma nova tarefa com a lista de lojas a serem restauradas.
		// Damos um delay inicial de 5 segundos para o servidor "respirar" ap�s ligar.
		ThreadPool.schedule(new RestoreTask(offlineShops), 5000L);
	}
	
	/**
	 * Uma classe interna que representa a tarefa de restaura��o. Ela processa as lojas em lotes para n�o sobrecarregar o servidor.
	 */
	private class RestoreTask implements Runnable
	{
		private final List<ShopObject> _shopsToRestore;
		private int _restoredCount;
		
		public RestoreTask(List<ShopObject> shops)
		{
			_shopsToRestore = shops;
			_restoredCount = 0;
		}
		
		@Override
		public void run()
		{
			int processed = 0;
			while (processed < OFFLINE_SHOPS_PER_TICK && !_shopsToRestore.isEmpty())
			{
				// Pega e remove a primeira loja da lista para processar.
				final ShopObject shopConfig = _shopsToRestore.remove(0);
				
				// A l�gica de cria��o do NPC permanece a mesma.
				if (World.getInstance().getPlayer(shopConfig.getOwnerId()) == null)
				{
					final Player sellerNpc = factory.createShopNpc(shopConfig);
					if (sellerNpc != null)
					{
						shopConfig.setSellerNpcObjectId(sellerNpc.getObjectId());
						shops.put(shopConfig.getOwnerId(), shopConfig);
						sellers.put(sellerNpc.getObjectId(), shopConfig.getOwnerId());
						
						sellerNpc.getManufactureList().setStoreName(shopConfig.getStoreMessage());
						sellerNpc.setPrivateStoreType(PrivateStoreType.MANUFACTURE);
						sellerNpc.spawnMe(shopConfig.getX(), shopConfig.getY(), shopConfig.getZ());
						World.getInstance().addPlayer(sellerNpc);
						sellerNpc.setOnlineStatus(true, false);
						sellerNpc.sitDown();
						sellerNpc.broadcastUserInfo();
						sellerNpc.broadcastPacket(new RecipeShopMsg(sellerNpc));
						
						_restoredCount++;
					}
				}
				processed++;
			}
			
			// Se ainda houver lojas na lista, reagenda a tarefa para o pr�ximo lote.
			if (!_shopsToRestore.isEmpty())
			{
				ThreadPool.schedule(this, OFFLINE_SHOPS_TICK_DELAY);
			}
			else
			{
				_log.info(String.format("BuffShopManager: Restaura��o conclu�da. Total de %d lojas de buffs offline carregadas.", _restoredCount));
			}
		}
	}
	
	/**
	 * Recompensa o dono da loja pela venda, delegando a l�gica de banco de dados para o DAO.
	 * @param shop O ShopObject da loja que realizou a venda.
	 * @param price O pre�o pelo qual o buff foi vendido.
	 */
	private void rewardSeller(final ShopObject shop, final int price)
	{
		if (price <= 0)
		{
			return;
		}
		
		final int ownerId = shop.getOwnerId();
		final Player owner = World.getInstance().getPlayer(ownerId);
		
		// Cen�rio 1: O dono da loja est� online.
		if (owner != null && owner.isOnline())
		{
			// owner.addAdena("BuffShopSell", price, null, true);
			owner.addAdena(price, true);
			owner.sendMessage("Seu buff foi vendido por " + price + " adena!");
		}
		// Cen�rio 2: O dono da loja est� offline.
		else
		{
			// A responsabilidade do Manager � decidir EXECUTAR a tarefa de forma ass�ncrona.
			// A responsabilidade do DAO � apenas EXECUTAR A QUERY.
			ThreadPool.execute(() -> dao.addAdenaToOfflinePlayer(ownerId, price));
		}
	}
	
	/**
	 * Processa a compra de uma skill permanente por um jogador. Valida todas as condi��es e, em caso de sucesso, adiciona a skill e consome os itens. Utiliza caixas de di�logo para notificar o jogador sobre o resultado.
	 * @param player O jogador que est� a comprar a skill.
	 * @param skillId O ID da skill a ser aprendida.
	 * @param levelToLearn O n�vel da skill a ser aprendido.
	 */
	public void buyPermanentSkill(Player player, int skillId, int levelToLearn)
	{
		// --- Valida��es Iniciais ---
		if (!BuffShopConfigs.BUFFSHOP_ALLOW_CLASS_SKILLSHOP.contains(player.getClassId().getId()))
		{
			BuffShopUIManager.getInstance().showSkillShopMessage(player, "A sua classe n�o pode usar esta loja.");
			return;
		}
		
		SkillPath skillPath = BuffShopConfigs.SKILL_SHOP_PATHS.get(skillId);
		if (skillPath == null || levelToLearn > skillPath.maxLevel())
		{
			BuffShopUIManager.getInstance().showSkillShopMessage(player, "Esta skill n�o est� dispon�vel<br1> para compra neste n�vel.");
			return;
		}
		
		if (player.getSkillLevel(skillId) >= levelToLearn)
		{
			BuffShopUIManager.getInstance().showSkillShopMessage(player, "Voc� j� aprendeu este n�vel da skill.");
			return;
		}
		
		// --- Valida��o e Consumo dos Custos ---
		List<Cost> costs = skillPath.costsByLevel().get(levelToLearn);
		if (costs == null || costs.isEmpty())
		{
			BuffShopUIManager.getInstance().showSkillShopMessage(player, "O custo n�o foi definido para<br1> este n�vel de skill.");
			return;
		}
		
		// Verifica se o jogador tem todos os itens necess�rios ANTES de consumir qualquer um.
		for (Cost cost : costs)
		{
			if (player.getInventory().getItemByItemId(cost.itemId()) == null || player.getInventory().getItemByItemId(cost.itemId()).getCount() < cost.count())
			{
				BuffShopUIManager.getInstance().showSkillShopMessage(player, "Voc� n�o possui os itens necess�rios<br1> para aprender esta skill.");
				return;
			}
		}
		
		// --- Sucesso: Consome os itens e adiciona a skill ---
		// Se passou por tudo, consome os itens e adiciona a skill
		for (Cost cost : costs)
		{
			player.destroyItemByItemId(cost.itemId(), cost.count(), true);
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(skillId, levelToLearn);
		player.addSkill(skill, true);
		
		// Envia a mensagem de sucesso atrav�s da caixa de di�logo.
		BuffShopUIManager.getInstance().showSkillShopMessage(player, "Voc� aprendeu<br><font color=LEVEL>" + skill.getName() + " N�vel " + levelToLearn + "</font>!");
		
		BuffShopUIManager.getInstance().showSkillShopWindow(player, 1); // Reabre a janela para mostrar a progress�o
	}
	
	public Map<Integer, ShopObject> getShops()
	{
		return shops;
	}
	
	public Map<Integer, Integer> getSellers()
	{
		return sellers;
	}
	
	public ShopObject getProfile(Player player)
	{
		return playerProfiles.computeIfAbsent(player.getObjectId(), ShopObject::new);
	}
}