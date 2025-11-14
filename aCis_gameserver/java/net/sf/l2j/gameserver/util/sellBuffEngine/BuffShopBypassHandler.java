package net.sf.l2j.gameserver.util.sellBuffEngine;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public final class BuffShopBypassHandler
{
	private static final Logger _log = Logger.getLogger(BuffShopBypassHandler.class.getName());
	
	private final BuffShopManager manager = BuffShopManager.getInstance();
	private final BuffShopUIManager uiManager = BuffShopUIManager.getInstance();
	
	private static class SingletonHolder
	{
		private static final BuffShopBypassHandler _instance = new BuffShopBypassHandler();
	}
	
	public static BuffShopBypassHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private BuffShopBypassHandler()
	{
	}
	
	public void handleBypass(Player player, String bypass)
	{
		if (!isPlayerStateValid(player))
			return;
		try
		{
			final StringTokenizer st = new StringTokenizer(bypass, " ");
			if (!st.hasMoreTokens())
				return;
			final String command = st.nextToken();
			switch (command)
			{
				case "index":
					uiManager.showIndexWindow(player, null);
					break;
				case "setshop":
					manager.startShopSetup(player);
					break;
				case "list":
					handleList(player, st);
					break;
				case "add":
					handleAddBuff(player, st);
					break;
				case "del":
					handleRemoveBuff(player, st);
					break;
				case "setprice":
					handleSetPrice(player, st);
					break;
				case "settitle":
					handleSetTitle(player, st);
					break;
				case "startshop":
					manager.startShop(player);
					break;
				case "stopshop":
					handleStopShop(player);
					break;
				case "showShop":
					handleShowShop(player, st);
					break;
				case "cast":
					handleCastBuff(player, st, false);
					break;
				case "manage_my_buffs":
					handleManageMyBuffs(player, st);
					break;
				case "remove_buff":
					handleRemoveActiveBuff(player, st);
					break;
				case "cast_confirm":
					handleConfirmedCast(player, st);
					break;
				case "shopskill":
					uiManager.showSkillShopWindow(player, 1);
					break;
				case "show_skill_shop":
					handleShowSkillShop(player, st);
					break;
				// ---------------------------------
				case "buy_skill":
					handleBuySkill(player, st);
					break;
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Erro ao processar bypass de BuffShop: " + bypass, e);
		}
	}
	
	/**
	 * NOVO M�TODO: Lida com a navega��o de p�ginas da loja de skills.
	 * @param player O jogador.
	 * @param st O StringTokenizer do bypass.
	 */
	private void handleShowSkillShop(Player player, StringTokenizer st)
	{
		try
		{
			// Se o bypass n�o tiver mais tokens (par�metros), vai para a p�gina 1.
			// Caso contr�rio, l� o n�mero da p�gina.
			int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			uiManager.showSkillShopWindow(player, page);
		}
		catch (NumberFormatException e)
		{
			// _log.warning("BuffShop: N�mero de p�gina inv�lido no bypass: " + st.toString());
			// Em caso de erro, volta para a primeira p�gina por seguran�a.
			uiManager.showSkillShopWindow(player, 1);
		}
	}
	
	private void handleShowShop(Player buyer, StringTokenizer st)
	{
		// _log.info("[HANDLE-SHOW-SHOP] ----- Iniciando handleShowShop para o comprador: " + buyer.getName() + " -----");
		
		if (!st.hasMoreTokens())
		{
			// _log.warning("[HANDLE-SHOW-SHOP] ERRO: Bypass n�o continha o ID do NPC.");
			return;
		}
		
		final int sellerNpcId = Integer.parseInt(st.nextToken());
		// _log.info("[HANDLE-SHOW-SHOP] 1. NpcID extra�do do bypass: " + sellerNpcId);
		
		final Player sellerNpc = World.getInstance().getPlayer(sellerNpcId);
		if (sellerNpc == null)
		{
			// _log.warning("[HANDLE-SHOW-SHOP] ERRO: NPC com ID " + sellerNpcId + " n�o foi encontrado no mundo (World.getInstance().getPlayer() retornou null).");
			return;
		}
		// _log.info("[HANDLE-SHOW-SHOP] 2. Objeto NPC encontrado no mundo: " + sellerNpc.getName());
		
		// --- PASSO 1: Traduzir o ID do NPC para o ID do Dono ---
		// _log.info("[HANDLE-SHOW-SHOP] 3. Buscando no mapa 'sellers' com a chave NpcID: " + sellerNpcId);
		final Integer ownerId = manager.getSellers().get(sellerNpcId);
		// _log.info("[HANDLE-SHOW-SHOP] 4. OwnerID retornado pelo mapa 'sellers': " + ownerId);
		
		if (ownerId == null)
		{
			buyer.sendMessage("Erro: O dono desta loja n�o foi encontrado.");
			// _log.warning("[HANDLE-SHOW-SHOP] ERRO: O mapa 'sellers' n�o cont�m uma entrada para o NpcID " + sellerNpcId);
			return;
		}
		
		// --- PASSO 2: Usar o ID do Dono para pegar o ShopObject ---
		// _log.info("[HANDLE-SHOW-SHOP] 5. Buscando no mapa 'shops' com a chave OwnerID: " + ownerId);
		final ShopObject shop = manager.getShops().get(ownerId);
		
		if (shop == null)
		{
			buyer.sendMessage("Erro: A configura��o da loja n�o foi encontrada.");
			// _log.warning("[HANDLE-SHOW-SHOP] ERRO: O mapa 'shops' n�o cont�m uma entrada para o OwnerID " + ownerId);
			return;
		}
		// _log.info("[HANDLE-SHOW-SHOP] 6. ShopObject encontrado! Detalhes -> OwnerID interno: " + shop.getOwnerId() + " | Total de buffs: " + shop.getBuffList().size());
		
		// --- PASSO 3: Processar o resto do bypass e chamar a UI ---
		int tabId = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
		int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
		// _log.info("[HANDLE-SHOW-SHOP] 7. Aba: " + tabId + " | P�gina: " + page);
		
		// _log.info("[HANDLE-SHOW-SHOP] 8. Todos os dados s�o v�lidos. Chamando UIManager para renderizar a janela.");
		uiManager.showPublicShopWindow(buyer, sellerNpc, shop, tabId, page);
		
		// _log.info("[HANDLE-SHOW-SHOP] ----- Finalizado handleShowShop para: " + buyer.getName() + " -----");
	}
	
	private void handleStopShop(Player player)
	{
		manager.stopShop(player);
		uiManager.showIndexWindow(player, "Sua loja foi fechada com sucesso.");
	}
	
	private void handleList(Player player, StringTokenizer st)
	{
		int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
		uiManager.showManagementWindow(player, page);
	}
	
	private void handleAddBuff(Player player, StringTokenizer st)
	{
		if (st.countTokens() < 4)
			return;
		final int skillId = Integer.parseInt(st.nextToken());
		final int skillLevel = Integer.parseInt(st.nextToken());
		final int price = Integer.parseInt(st.nextToken());
		final int page = Integer.parseInt(st.nextToken());
		manager.addBuffToProfile(player, skillId, skillLevel, price);
		uiManager.showManagementWindow(player, page);
	}
	
	private void handleRemoveBuff(Player player, StringTokenizer st)
	{
		if (st.countTokens() < 2)
			return;
		final int skillId = Integer.parseInt(st.nextToken());
		final int page = Integer.parseInt(st.nextToken());
		manager.removeBuffFromProfile(player, skillId);
		uiManager.showManagementWindow(player, page);
	}
	
	private void handleSetPrice(Player player, StringTokenizer st)
	{
		if (!st.hasMoreTokens())
			return;
		final String price = st.nextToken();
		final int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
		if (isDigit(price))
		{
			manager.getProfile(player).setTempPrice(price);
		}
		uiManager.showManagementWindow(player, page);
	}
	
	private void handleSetTitle(Player player, StringTokenizer st)
	{
		final String title = st.hasMoreTokens() ? st.nextToken("").trim() : "";
		if (!title.isEmpty() && title.length() < 30)
		{
			manager.getProfile(player).setTitle(title);
		}
		uiManager.showManagementWindow(player, 1);
	}
	
	private void handleCastBuff(Player buyer, StringTokenizer st, boolean isConfirmed)
	{
		
		if (st.countTokens() < 4)
			return;
		final int sellerNpcId = Integer.parseInt(st.nextToken());
		final int buffId = Integer.parseInt(st.nextToken());
		final int buffLevel = Integer.parseInt(st.nextToken());
		final int activeTab = Integer.parseInt(st.nextToken());
		final String target = st.hasMoreTokens() ? st.nextToken() : "player";
		final int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
		
		final Player sellerNpc = World.getInstance().getPlayer(sellerNpcId);
		final L2Skill skill = SkillTable.getInstance().getInfo(buffId, buffLevel);
		
		final L2Skill newSkill = SkillTable.getInstance().getInfo(buffId, buffLevel);
		
		// --- BUSCA CORRETA DA LOJA ---
		final Integer ownerId = manager.getSellers().get(sellerNpcId);
		if (ownerId == null)
			return;
		final ShopObject shop = manager.getShops().get(ownerId);
		// -----------------------------
		
		if (sellerNpc == null || manager.getSellers().get(sellerNpcId) == null)
		{
			buyer.sendMessage("O vendedor n�o est� mais dispon�vel.");
			return;
		}
		if (manager.getSellers().get(sellerNpcId).equals(buyer.getObjectId()))
		{
			buyer.sendMessage("Voc� n�o pode comprar de sua pr�pria loja.");
			return;
		}
		if (!checkIfInRange(150, sellerNpc, buyer, true))
		{
			buyer.sendMessage("Voc� est� muito longe da loja.");
			// Calcule um novo local pr�ximo � loja
			final Location destination = GeoEngine.getInstance().getValidLocation(buyer, sellerNpc.getX(), sellerNpc.getY(), sellerNpc.getZ());
			// Defina a inten��o do jogador para se mover para o novo local
			buyer.getAI().doMoveToIntention(destination, null);
			// uiManager.showManagementWindow(buyer, 1);
			handleShowShop(buyer, st);
			// BuffShopUIManager.getInstance().showPublicShopWindow(buyer, sellerNpc, BuffShopManager.getInstance().getProfile(sellerNpc), 1, 1);
			buyer.sendPacket(ActionFailed.STATIC_PACKET); // Importante para o cliente n�o ficar "preso"
			
			return; // Interrompa o m�todo para que o jogador possa se mover
		}
		// --- VALIDA��O DE CUSTOS E CONDI��ES DA SKILL (A PARTE CR�TICA) ---
		
		boolean isSummon = BuffShopConfigs.BUFFSHOP_RESTRICTED_SUMMONS.contains(buffId);
		boolean isCubic = BuffShopConfigs.BUFFSHOP_ALLOWED_SELF_SKILLS.contains(buffId);
		boolean isReplaceable = BuffShopConfigs.BUFFSHOP_REPLACEABLE_BUFFS.contains(buffId);
		
		if (isSummon || isCubic)
		{
			// Valida se o comprador j� tem um pet, se for um summon
			if (isSummon && buyer.getSummon() != null)
			{
				
				buyer.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
				return;
			}
			// Valida se o comprador tem MP suficiente
			
			if (buyer.getStatus().getMp() < skill.getMpConsume())
			{
				
				buyer.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
				return;
			}
			// Valida se o comprador tem os itens (cristais) necess�rios
			if (skill.getItemConsumeId() > 0 && buyer.getInventory().getItemByItemId(skill.getItemConsumeId()) == null)
			{
				
				buyer.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
			
		}
		
		// --- NOVA L�GICA PARA BUFFS SUBSTITU�VEIS ---
		// 1. Verifica se a skill que o jogador quer comprar � uma da lista de substitu�veis.
		if (BuffShopConfigs.BUFFSHOP_REPLACEABLE_BUFFS.contains(buffId))
		{
			// 2. Itera sobre TODAS as skills substitu�veis para ver se o jogador j� tem ALGUMA delas.
			for (int replaceableId : BuffShopConfigs.BUFFSHOP_REPLACEABLE_BUFFS)
			{
				
				// Pega o objeto da loja do vendedor.
				
				AbstractEffect oldEffect = buyer.getFirstEffect(replaceableId);
				if (oldEffect != null)
				{
					// --- COLETA DE DADOS OCORRE AQUI, AP�S TER O 'shop' CORRETO ---
					L2Skill oldSkill = oldEffect.getSkill();
					
					// Pega o custo em Adena do buff novo a partir do 'shop' correto
					final ShopObject.PrivateBuff buffInfo = shop.getBuff(buffId);
					if (buffInfo == null)
					{
						// Se o buff n�o est� na loja, algo est� errado, aborta.
						return;
					}
					final long adenaCost = buffInfo.price();
					
					// Constr�i o bypass para confirma��o
					final String confirmBypass = String.format("bypass -h cast_confirm %d %d %d %s %d", sellerNpcId, buffId, buffLevel, target, page);
					
					// Delega para o UIManager com todos os dados j� coletados
					uiManager.showBuffReplaceConfirmation(buyer, sellerNpc, oldSkill, newSkill, adenaCost, confirmBypass, activeTab, page);
					return;
				}
			}
		}
		
		// Chama o Manager, agora passando o ID e o N�VEL da skill.
		manager.sellBuff(sellerNpc, buyer, buffId, buffLevel, target, page);
	}
	
	// Em BuffShopBypassHandler
	private void handleBuySkill(Player player, StringTokenizer st)
	{
		if (st.countTokens() < 2)
			return;
		int skillId = Integer.parseInt(st.nextToken());
		int skillLevel = Integer.parseInt(st.nextToken());
		// A valida��o agora est� toda dentro do Manager
		manager.buyPermanentSkill(player, skillId, skillLevel);
	}
	
	/**
	 * Lida com a resposta afirmativa do jogador.
	 */
	private void handleConfirmedCast(Player buyer, StringTokenizer st)
	{
		if (st.countTokens() < 5)
			return;
		
		final int sellerNpcId = Integer.parseInt(st.nextToken());
		final int buffId = Integer.parseInt(st.nextToken());
		final int buffLevel = Integer.parseInt(st.nextToken());
		final String target = st.nextToken();
		final int page = Integer.parseInt(st.nextToken());
		
		final Player sellerNpc = World.getInstance().getPlayer(sellerNpcId);
		if (sellerNpc == null)
			return;
		
		// Remove todos os buffs substitu�veis para "limpar o terreno".
		for (int replaceableId : BuffShopConfigs.BUFFSHOP_REPLACEABLE_BUFFS)
		{
			buyer.stopSkillEffects(replaceableId);
		}
		// Remove tamb�m o cubic (stopSkillEffects funciona para o ID da skill principal)
		buyer.stopSkillEffects(buffId);
		
		// Prossegue com a compra.
		manager.sellBuff(sellerNpc, buyer, buffId, buffLevel, target, page);
	}
	
	/**
	 * Envia o di�logo de confirma��o com um bypass V�LIDO.
	 */
	private void sendConfirmationDialog(Player player, Player sellerNpc, String question, int sellerNpcId, int buffId, int buffLevel, String target, int page)
	{
		// Constr�i a string de bypass manualmente com os par�metros corretos.
		final String confirmBypass = String.format("bypass -h cast_confirm %d %d %d %s %d", sellerNpcId, buffId, buffLevel, target, page);
		final String cancelBypass = "bypass -h Dialog 1"; // Simplesmente fecha a janela.
		
		final NpcHtmlMessage html = new NpcHtmlMessage(sellerNpc.getObjectId());
		html.setHtml("<html><body>" + question + "<br><center>" + "<button value=\"Sim\" action=\"" + confirmBypass + "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">" + "&nbsp;<button value=\"N�o\" action=\"" + cancelBypass + "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">" + "</center></body></html>");
		player.sendPacket(html);
	}
	
	private void handleManageMyBuffs(Player player, StringTokenizer st)
	{
		String targetType = st.hasMoreTokens() ? st.nextToken() : "player";
		uiManager.showBuffRemovalWindow(player, targetType);
	}
	
	private void handleRemoveActiveBuff(Player player, StringTokenizer st)
	{
		if (st.countTokens() < 3)
			return;
		final int skillId = Integer.parseInt(st.nextToken());
		final int skillLevel = Integer.parseInt(st.nextToken());
		final String targetType = st.nextToken();
		uiManager.removePlayerBuff(player, skillId, skillLevel, targetType);
	}
	
	private boolean isPlayerStateValid(Player player)
	{
		if (player == null || player.isInStoreMode() || player.isDead() || player.isInCombat())
		{
			player.sendMessage("Voc� n�o pode fazer isso agora.");
			return false;
		}
		return true;
	}
	
	private boolean checkIfInRange(int range, WorldObject obj1, WorldObject obj2, boolean includeZ)
	{
		if (obj1 == null || obj2 == null || range == -1)
			return true;
		long dx = obj1.getX() - obj2.getX();
		long dy = obj1.getY() - obj2.getY();
		long dz = includeZ ? obj1.getZ() - obj2.getZ() : 0;
		return dx * dx + dy * dy + dz * dz <= (long) range * range;
	}
	
	private boolean isDigit(String text)
	{
		return text != null && !text.isEmpty() && text.matches("\\d+");
	}
}