package net.sf.l2j.gameserver.util.sellBuffEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.xml.IconTable;
import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.data.xml.PlayerData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.util.sellBuffEngine.BuffShopConfigs.Cost;
import net.sf.l2j.gameserver.util.sellBuffEngine.BuffShopConfigs.SkillPath;
import net.sf.l2j.gameserver.util.sellBuffEngine.ShopObject.PrivateBuff;

/*
L2jbrasil Dhousefe
*/
/**
 * Respons�vel por toda a gera��o de HTML para a interface do Buff Shop. Esta classe � stateless e seus m�todos s�o utilit�rios para criar e enviar as janelas (NpcHtmlMessage) para o cliente.
 */
public final class BuffShopUIManager
{
	
	// --- Constantes ---
	// Defina quantas skills ser�o exibidas por p�gina para gerenciar skills.
	private static final int SKILLS_PER_PAGE_MANAGEMENT = 5;
	// Defina quantas skills ser�o exibidas por p�gina para comprar buffs.
	private static final int BUFFS_PER_PAGE_PUBLIC = 6;
	// Defina quantas skills ser�o exibidas por p�gina para remover skills.
	private static final int BUFF_REMOVAL_COLUMNS = 7;
	// Defina quantas skills ser�o exibidas por p�gina para aprender skills.
	private static final int SKILLS_PER_PAGE = 5;
	
	// --- Singleton ---
	private static class SingletonHolder
	{
		private static final BuffShopUIManager _instance = new BuffShopUIManager();
	}
	
	public static BuffShopUIManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private BuffShopUIManager()
	{
		// Construtor privado para garantir o padr�o Singleton.
	}
	
	private static final Logger _log = Logger.getLogger(BuffShopUIManager.class.getName());
	
	// ============================================================================================
	// 1. M�TODOS P�BLICOS (API DA CLASSE)
	// ============================================================================================
	
	/**
	 * Exibe a janela de �ndice principal do sistema.
	 * @param player O jogador que receber� a janela
	 * @param message Mensagem opcional a ser exibida na janela
	 */
	public void showIndexWindow(Player player, String message)
	{
		String html = HtmCache.getInstance().getHtm("./data/html/BuffShop/index.htm");
		html = html.replace("%message%", (message != null) ? message : "");
		sendHtml(player, html);
	}
	
	/**
	 * [REESCRITO PARA USAR SkillCache] Exibe a janela de gerenciamento da loja, agora utilizando a SkillCache para performance.
	 */
	public void showManagementWindow(final Player player, final int page)
	{
		// 1. Prepara o jogador, concedendo skills tempor�rias com base nas configura��es do .ini
		preparePlayerSkillsForShop(player);
		
		// 2. Obt�m a lista de todas as skills que o jogador pode vender (suas pr�prias + as tempor�rias)
		final List<L2Skill> allSellableSkills = getSellableSkills(player);
		
		// 2. Calcula a pagina��o de forma segura.
		final int totalPages = Math.max((int) Math.ceil((double) allSellableSkills.size() / SKILLS_PER_PAGE_MANAGEMENT), 1);
		final int currentPage = Math.max(1, Math.min(page, totalPages));
		
		// 3. Pega apenas a "fatia" da lista correspondente � p�gina atual.
		final List<L2Skill> skillsForThisPage = paginate(allSellableSkills, currentPage, SKILLS_PER_PAGE_MANAGEMENT);
		
		// 4. Obt�m o perfil de loja do jogador para saber o que j� foi adicionado.
		final ShopObject shopConfig = BuffShopManager.getInstance().getProfile(player);
		
		// 5. Constr�i os componentes da UI.
		final String skillListHtml = buildManagementSkillList(skillsForThisPage, shopConfig, currentPage);
		final String[] navigation = buildManagementPageNavigation("list", currentPage, totalPages);
		
		// 6. Monta e envia o HTML final.
		String html = HtmCache.getInstance().getHtm("./data/html/BuffShop/shopBuffList.htm");
		
		html = html.replace("%shop_title%", shopConfig.getTitle());
		html = html.replace("%shop_buff_count%", String.valueOf(shopConfig.getBuffList().size()));
		html = html.replace("%max_buff_count%", String.valueOf(BuffShopConfigs.BUFFSHOP_BUFFS_MAX_COUNT));
		html = html.replace("%,d", String.format("%,d", Integer.parseInt(shopConfig.getTempPrice())));
		
		html = html.replace("%skill_list%", skillListHtml);
		html = html.replace("%prev_button%", navigation[0]);
		html = html.replace("%page_info%", navigation[1]);
		html = html.replace("%next_button%", navigation[2]);
		
		sendHtml(player, html);
	}
	
	/**
	 * Retorna uma "fatia" paginada de uma lista maior.
	 * @param fullList A lista completa a ser paginada.
	 * @param page O n�mero da p�gina desejada (come�ando em 1).
	 * @param itemsPerPage O n�mero de itens por p�gina.
	 * @return Uma sub-lista contendo apenas os itens da p�gina especificada.
	 */
	private static <T> List<T> paginate(List<T> fullList, int page, int itemsPerPage)
	{
		if (fullList == null || fullList.isEmpty())
		{
			return List.of(); // Retorna uma lista vazia imut�vel
		}
		
		final int startIndex = (page - 1) * itemsPerPage;
		
		// Checagem de seguran�a para evitar IndexOutOfBoundsException
		if (startIndex >= fullList.size())
		{
			return List.of();
		}
		
		final int endIndex = Math.min(startIndex + itemsPerPage, fullList.size());
		return fullList.subList(startIndex, endIndex);
	}
	
	// Em BuffShopUIManager.java
	
	public void showPublicShopWindow(final Player buyer, final Player sellerNpc, final ShopObject shop, int activeTab, int currentPage)
	{
		// _log.info("[DEBUG-UI-1] Entrou em showPublicShopWindow para a loja do owner: " + shop.getOwnerId());
		// _log.info("[DEBUG-UI-2] O objeto 'shop' recebido cont�m " + shop.getBuffList().size() + " buffs no total.");
		
		// 1. Filtra os buffs que devem ser exibidos na aba atual.
		// final List<PrivateBuff> buffsForTab = getFilteredBuffsForTab(shop, activeTab);
		
		final List<PrivateBuff> buffsForTab = getFilteredBuffsForTab(shop, activeTab, buyer); // <-- MUDAN�A AQUI
		// _log.info("[DEBUG-UI-3] Ap�s filtrar para a aba " + activeTab + ", restam " + buffsForTab.size() + " buffs.");
		
		// 2. Calcula a pagina��o.
		final int totalPages = Math.max((int) Math.ceil((double) buffsForTab.size() / BUFFS_PER_PAGE_PUBLIC), 1);
		currentPage = Math.max(1, Math.min(currentPage, totalPages));
		// _log.info("[DEBUG-UI-4] Pagina��o calculada. Total de p�ginas: " + totalPages + ", P�gina atual: " + currentPage);
		
		// 3. Pega a "fatia" da lista para a p�gina atual.
		final List<PrivateBuff> buffsForPage = paginate(buffsForTab, currentPage, BUFFS_PER_PAGE_PUBLIC);
		// _log.info("[DEBUG-UI-5] Ap�s paginar, a lista para exibir na tela cont�m " + buffsForPage.size() + " buffs.");
		
		// 4. Constr�i o HTML usando os m�todos auxiliares.
		final StringBuilder html = new StringBuilder("<html><body>");
		
		buildShopHeader(html, sellerNpc, shop);
		buildShopTabs(html, sellerNpc.getObjectId(), activeTab, currentPage);
		
		String pageNavBypass = String.format("showShop %d %d", sellerNpc.getObjectId(), activeTab);
		html.append(buildPageNavigation(pageNavBypass, currentPage, totalPages));
		
		// Se este m�todo receber uma lista vazia, ele imprimir� "Nenhum buff dispon�vel...".
		// html.append(buildShopBuffList(buffsForPage, sellerNpc.getObjectId(), activeTab, currentPage));
		// Passa o 'buyer' para o m�todo de constru��o da lista
		html.append(buildShopBuffList(buffsForPage, sellerNpc.getObjectId(), activeTab, currentPage, buyer));
		// ...
		
		buildShopFooter(html);
		
		html.append("</body></html>");
		// _log.info("[DEBUG-UI-6] HTML final constru�do. Enviando para o jogador " + buyer.getName());
		sendHtml(buyer, html.toString());
	}
	
	/**
	 * Exibe a janela para o jogador gerenciar (remover) seus buffs ativos ou os de seu pet.
	 */
	public void showBuffRemovalWindow(final Player player, final String targetType)
	{
		final Creature target = determineTargetForRemoval(player, targetType);
		if (target == null)
		{
			player.sendMessage("Voc� n�o tem um pet/summon ativo para gerenciar.");
			showBuffRemovalWindow(player, "player");
			return;
		}
		
		String template = HtmCache.getInstance().getHtm("./data/html/mods/BuffManager.htm");
		if (template == null)
			return;
		
		final String tabsHtml = buildRemovalWindowTabs(targetType);
		final String buffGridHtml = buildBuffIconGrid(target, targetType);
		
		template = template.replace("%tabs%", tabsHtml);
		template = template.replace("%buff_list%", buffGridHtml);
		template = template.replace("%update_bypass%", "manage_my_buffs " + targetType);
		
		sendHtml(player, template);
	}
	
	/**
	 * Processa a remo��o de um efeito de buff ativo.
	 */
	public void removePlayerBuff(Player player, int skillId, int skillLevel, String targetType)
	{
		final Creature target = determineTargetForRemoval(player, targetType);
		if (target == null)
			return;
		
		Arrays.stream(target.getAllEffects()).filter(effect -> effect.getSkill().getId() == skillId && effect.getSkill().getLevel() == skillLevel).findFirst().ifPresent(AbstractEffect::exit);
		
		showBuffRemovalWindow(player, targetType);
	}
	
	// ============================================================================================
	// 2. M�TODOS AUXILIARES PARA PREPARA��O DE DADOS
	// ============================================================================================
	
	private void preparePlayerSkillsForShop(Player player)
	{
		final String baseClassName = ClassId.values()[player.getBaseClass()].name();
		if (BuffShopConfigs.BUFFSHOP_CLASS_SPECIFIC_SKILLS.containsKey(baseClassName))
		{
			for (int skillId : BuffShopConfigs.BUFFSHOP_CLASS_SPECIFIC_SKILLS.get(baseClassName))
			{
				grantHighestAvailableLevel(player, skillId);
			}
		}
		
		final ClassId baseClass = ClassId.values()[player.getBaseClass()];
		final int playerLevel = player.getStatus().getLevel();
		if (BuffShopConfigs.BUFFSHOP_GRANT_SKILLS.containsKey(baseClass))
		{
			BuffShopConfigs.BUFFSHOP_GRANT_SKILLS.get(baseClass).stream().collect(Collectors.groupingBy(BuffShopConfigs.SkillGrantRule::skillId)).forEach((skillId, rules) -> rules.stream().filter(rule -> playerLevel >= rule.requiredLevel()).max(Comparator.comparingInt(BuffShopConfigs.SkillGrantRule::skillLevel)).ifPresent(bestRule ->
			{
				L2Skill skillToAdd = SkillTable.getInstance().getInfo(bestRule.skillId(), bestRule.skillLevel());
				if (skillToAdd != null && player.getSkillLevel(skillId) < bestRule.skillLevel())
				{
					player.addSkill(skillToAdd, false);
				}
			}));
		}
	}
	
	private void grantHighestAvailableLevel(Player player, int skillId)
	{
		final int playerLevel = player.getStatus().getLevel();
		final int maxLevel = player.getTemplate().getSkills().stream().filter(s -> s.getId() == skillId && s.getMinLvl() <= playerLevel).mapToInt(s -> s.getValue()).max().orElse(0);
		
		if (maxLevel > 0)
		{
			L2Skill skillToAdd = SkillTable.getInstance().getInfo(skillId, maxLevel);
			if (skillToAdd != null && player.getSkillLevel(skillId) < maxLevel)
			{
				player.addSkill(skillToAdd, false);
			}
		}
	}
	
	private List<L2Skill> getSellableSkills(Player player)
	{
		return player.getSkills().values().stream().filter(this::checkBuffCondition).sorted(Comparator.comparing(L2Skill::getName)).collect(Collectors.toList());
	}
	
	/**
	 * Filtra a lista de buffs de uma loja para a aba espec�fica (Player ou Pet). Esta implementa��o usa Streams para clareza, mas mant�m a l�gica exata do seu c�digo antigo.
	 * @param shop O objeto da loja contendo os buffs
	 * @param activeTab A aba ativa (1 para Player, 2 para Pet)
	 * @return Lista filtrada de PrivateBuff baseada na aba selecionada
	 */
	private List<PrivateBuff> getFilteredBuffsForTab(ShopObject shop, int activeTab, Player buyer)
	{
		final List<PrivateBuff> resultingList = new ArrayList<>();
		
		for (final PrivateBuff buffEntry : shop.getBuffList().values())
		{
			final L2Skill sellerSkill = SkillTable.getInstance().getInfo(buffEntry.skillId(), buffEntry.skillLevel());
			if (sellerSkill == null)
			{
				continue;
			}
			
			// --- L�GICA DE FILTRO INTELIGENTE PARA SUMMONS ---
			// Verifica se � um summon restrito
			if (BuffShopConfigs.BUFFSHOP_RESTRICTED_SUMMONS.contains(sellerSkill.getId()))
			{
				// Procura o n�vel m�ximo que o COMPRADOR poderia ter se fosse um summoner.
				int appropriateLevel = findAppropriateSummonLevel(sellerSkill.getId(), buyer.getStatus().getLevel());
				
				// Se um n�vel apropriado foi encontrado (maior que 0)
				if (appropriateLevel > 0)
				{
					// Adiciona uma NOVA vers�o do buff � lista, com o n�vel rebaixado mas o pre�o original.
					resultingList.add(new PrivateBuff(buffEntry.price(), buffEntry.skillId(), appropriateLevel));
				}
				// Se nenhum n�vel for encontrado (o comprador tem n�vel muito baixo), o buff � omitido.
				continue; // Pula para o pr�ximo buff da loja.
			}
			// --- FIM DA L�GICA DE SUMMONS ---
			
			// L�gica de filtro padr�o para buffs normais (Player/Pet)
			if (activeTab == 1)
			{ // Aba Player
				if (sellerSkill.getTargetType() != SkillTargetType.SUMMON)
				{
					resultingList.add(buffEntry);
				}
			}
			else
			{ // Aba Pet
				if (!BuffShopConfigs.BUFFSHOP_ALLOWED_SELF_SKILLS.contains(sellerSkill.getId()))
				{
					resultingList.add(buffEntry);
				}
			}
		}
		
		// Ordena a lista final pelo pre�o.
		resultingList.sort(Comparator.comparingInt(PrivateBuff::price));
		return resultingList;
	}
	
	/**
	 * Novo m�todo auxiliar para encontrar o n�vel de summon apropriado para o n�vel do comprador. Ele verifica as classes de summoner de refer�ncia.
	 * @param skillId O ID da skill de summon.
	 * @param buyerLevel O n�vel do jogador comprador.
	 * @return O n�vel m�ximo da skill que um summoner do mesmo n�vel poderia aprender, ou 0 se nenhum.
	 */
	private int findAppropriateSummonLevel(int skillId, int buyerLevel)
	{
		int maxLvlFound = 0;
		
		// Itera sobre as classes de summoner de refer�ncia (Warlock, etc.)
		for (ClassId refClassId : BuffShopConfigs.REFERENCE_SUMMONER_CLASSES)
		{
			final PlayerTemplate template = PlayerData.getInstance().getTemplate(refClassId);
			if (template == null)
				continue;
				
			// Encontra o n�vel m�ximo daquela skill que um personagem da classe de refer�ncia
			// poderia ter no n�vel do comprador.
			int levelForThisClass = template.getSkills().stream().filter(s -> s.getId() == skillId && s.getMinLvl() <= buyerLevel).mapToInt(s -> s.getValue()).max().orElse(0);
			
			// Mant�m o maior n�vel encontrado entre todas as classes de refer�ncia.
			if (levelForThisClass > maxLvlFound)
			{
				maxLvlFound = levelForThisClass;
			}
		}
		
		return maxLvlFound;
	}
	
	private Creature determineTargetForRemoval(Player player, String targetType)
	{
		return "pet".equalsIgnoreCase(targetType) ? player.getSummon() : player;
	}
	
	// ============================================================================================
	// 3. M�TODOS AUXILIARES PARA CONSTRU��O DE HTML
	// ============================================================================================
	
	/**
	 * Constr�i o HTML da tabela com a lista de skills para a janela de gerenciamento.
	 * @param skills A lista de skills para exibir nesta p�gina.
	 * @param shopConfig O perfil da loja do jogador, para verificar quais buffs j� foram adicionados.
	 * @param page O n�mero da p�gina atual, para ser usado nos bypasses de a��o.
	 * @return Uma string contendo as tags
	 *         <tr>
	 *         para a lista de skills.
	 */
	private static String buildManagementSkillList(List<L2Skill> skills, ShopObject shopConfig, int page)
	{
		if (skills.isEmpty())
		{
			return "<tr><td colspan=3 align=center>Nenhuma skill dispon�vel para venda para sua classe.</td></tr>";
		}
		final StringBuilder sb = new StringBuilder();
		for (final L2Skill skill : skills)
		{
			final boolean isAlreadyAdded = shopConfig.getBuff(skill.getId()) != null;
			final String priceHtml = isAlreadyAdded ? String.format("<br><font color=FE2E2E>Pre�o: %,d</font>", shopConfig.getBuff(skill.getId()).price()) : "<br><font color=AE9978>N�o adicionado</font>";
			final String actionButtonHtml = isAlreadyAdded ? String.format("<button value=\" \" action=\"bypass -h del %d %d\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout1_over\" fore=\"L2UI_CH3.mapbutton_zoomout1\">", skill.getId(), page) : String.format("<button value=\" \" action=\"bypass -h add %d %d %s %d\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin1_over\" fore=\"L2UI_CH3.mapbutton_zoomin1\">", skill.getId(), skill.getLevel(), shopConfig.getTempPrice(), page);
			
			sb.append(String.format("""
				<tr>
				<td width=40 align=center><img src="%s" width=32 height=32></td>
				<td width=160 align=left>%s%s</td>
				<td width=60 align=center>%s</td>
				</tr>
				""", "Icon.skill" + getIconSkill(skill.getId()), skill.getName(), priceHtml, actionButtonHtml));
		}
		return sb.toString();
	}
	
	/**
	 * [VERS�O FINAL 4.0] Constr�i o HTML da tabela com a lista de buffs. Para summons, ele calcula o n�vel apropriado para o comprador e o insere no bypass de compra.
	 */
	/**
	 * Constr�i o HTML da tabela com a lista de buffs da p�gina atual.
	 * @param buffsForPage Lista de buffs a serem exibidos na p�gina atual.
	 * @param sellerId ID do NPC vendedor.
	 * @param activeTab Aba ativa (1 para player, 2 para pet).
	 * @param page N�mero da p�gina atual.
	 * @param buyer O jogador que est� visualizando/comprando os buffs.
	 * @return Uma String contendo o HTML da tabela de buffs.
	 */
	private String buildShopBuffList(List<PrivateBuff> buffsForPage, int sellerId, int activeTab, int page, Player buyer)
	{
		final StringBuilder sb = new StringBuilder("<table width=280 border=0>");
		
		if (buffsForPage.isEmpty())
		{
			sb.append("<tr><td align=center>Nenhum buff dispon�vel nesta categoria.</td></tr>");
		}
		else
		{
			for (final PrivateBuff buffEntry : buffsForPage)
			{
				final L2Skill originalSkill = SkillTable.getInstance().getInfo(buffEntry.skillId(), buffEntry.skillLevel());
				if (originalSkill == null)
					continue;
				
				L2Skill skillToShow = originalSkill; // Por padr�o, a skill a ser mostrada � a original.
				int levelToBuy = originalSkill.getLevel();
				
				// L�gica especial para summons restritos
				if (BuffShopConfigs.BUFFSHOP_RESTRICTED_SUMMONS.contains(originalSkill.getId()))
				{
					int appropriateLevel = findAppropriateSummonLevel(originalSkill.getId(), buyer.getStatus().getLevel());
					
					if (appropriateLevel > 0)
					{
						levelToBuy = appropriateLevel;
						skillToShow = SkillTable.getInstance().getInfo(originalSkill.getId(), levelToBuy);
					}
					else
					{
						continue; // Pula a exibi��o deste summon, pois o comprador n�o tem n�vel nem para o lv 1.
					}
				}
				
				final String targetType = (activeTab == 1) ? "player" : "pet";
				final String image = "Icon.skill" + getIconSkill(skillToShow.getId());
				
				// ** A MUDAN�A CR�TICA EST� AQUI **
				// O bypass agora envia o ID da skill E o N�VEL CORRETO a ser comprado.
				// final String buyBypass = String.format("bypass -h cast %d %d %d %s %d", sellerId, skillToShow.getId(), levelToBuy, targetType, page);
				
				// --- BYPASS CORRIGIDO E FINAL ---
				// Formato: cast sellerId skillId skillLevel activeTab targetType page
				final String buyBypass = String.format("bypass -h cast %d %d %d %d %s %d", sellerId, skillToShow.getId(), levelToBuy, activeTab, targetType, page);
				
				final String description = buildSkillDescription(skillToShow, buffEntry); // A descri��o mostra o n�vel correto
				final String buyButton = String.format("<button action=\"%s\" value=\"Comprar\" width=60 height=25 back=\"L2butom.bitbuttom8_over\" fore=\"L2butom.bitbuttom8\">", buyBypass);
				
				sb.append(String.format("""
					<tr>
					<td width=34><button action="%s" width=32 height=32 back="%s" fore="%s"></td>
					<td width=186 align=left>%s</td>
					<td width=60 align=center>%s</td>
					</tr>
					""", buyBypass, image, image, description, buyButton));
			}
		}
		sb.append("</table>");
		return sb.toString();
	}
	
	private String buildPageNavigation(String bypassCommand, int currentPage, int totalPages)
	{
		if (totalPages <= 1)
			return "";
		final String prev = (currentPage > 1) ? String.format("<button width=32 height=19 back=\"L2UI_CH3.calculate2_sub_down\" fore=\"L2UI_CH3.calculate2_sub_over\" action=\"bypass -h %s %d\">", bypassCommand, currentPage - 1) : "";
		final String next = (currentPage < totalPages) ? String.format("<button width=32 height=19 back=\"L2UI_CH3.calculate2_add_down\" fore=\"L2UI_CH3.calculate2_add_over\" action=\"bypass -h %s %d\">", bypassCommand, currentPage + 1) : "";
		return String.format("<table width=270><tr><td align=left width=100>%s</td><td align=center width=70>P�g %d/%d</td><td align=right width=100>%s</td></tr></table>", prev, currentPage, totalPages, next);
	}
	
	/**
	 * Constr�i o cabe�alho da janela da loja.
	 */
	private void buildShopHeader(StringBuilder sb, Player sellerNpc, ShopObject shop)
	{
		sb.append("<center><title>").append(shop.getTitle()).append("</title><br1>");
		sb.append("Vendedor: <font color=LEVEL>").append(sellerNpc.getName()).append("</font><br1>");
		sb.append("Mana: <font color=00FFFF>").append((int) sellerNpc.getStatus().getMp()).append(" / ").append(sellerNpc.getStatus().getMaxMp()).append("</font>");
		sb.append("<br1><img src=\"L2UI.SquareGray\" width=270 height=1><br1>");
	}
	
	/**
	 * Constr�i as abas de navega��o (Player/Pet) para a janela p�blica da loja, com estilo visual para a aba ativa e inativa.
	 */
	private static void buildShopTabs(StringBuilder sb, int sellerId, int activeTab, int page)
	{
		final boolean isPlayerTabActive = (activeTab == 1);
		
		// Define o estilo para o bot�o do Player
		final String playerButtonValue = isPlayerTabActive ? "(Buffs Player)" : "Buffs Player";
		final String playerButtonBack = isPlayerTabActive ? "L2butom.bitbuttom8" : "L2butom.bitbuttom8_over";
		final String playerButtonAction = isPlayerTabActive ? "" : String.format("bypass -h showShop %d 1 %d", sellerId, page);
		
		// Define o estilo para o bot�o do Pet
		final String petButtonValue = !isPlayerTabActive ? "(Buffs Pet)" : "Buffs Pet";
		final String petButtonBack = !isPlayerTabActive ? "L2butom.bitbuttom8" : "L2butom.bitbuttom8_over";
		final String petButtonAction = !isPlayerTabActive ? "" : String.format("bypass -h showShop %d 2 %d", sellerId, page);
		
		// Monta o HTML final da tabela
		sb.append(String.format("""
			<table width=270><tr>
			<td align=center><button value="%s" action="%s" width=130 height=19 back="%s" fore="L2butom.bitbuttom8"></td>
			<td align=center><button value="%s" action="%s" width=130 height=19 back="%s" fore="L2butom.bitbuttom8"></td>
			</tr></table>
			""", playerButtonValue, playerButtonAction, playerButtonBack, petButtonValue, petButtonAction, petButtonBack));
	}
	
	/**
	 * Constr�i as abas de navega��o para a janela de remo��o de buffs, com estilo visual para a aba ativa e inativa.
	 */
	private String buildRemovalWindowTabs(String activeTarget)
	{
		final boolean isPlayerTabActive = "player".equals(activeTarget);
		
		// Define o estilo para o bot�o "Meus Buffs"
		final String playerButtonValue = isPlayerTabActive ? "(Meus Buffs)" : "Meus Buffs";
		final String playerButtonBack = isPlayerTabActive ? "L2butom.bitbuttom8" : "L2butom.bitbuttom8_over";
		final String playerButtonAction = isPlayerTabActive ? "" : "bypass -h manage_my_buffs player";
		
		// Define o estilo para o bot�o "Buffs do Pet"
		final String petButtonValue = !isPlayerTabActive ? "(Buffs do Pet)" : "Buffs do Pet";
		final String petButtonBack = !isPlayerTabActive ? "L2butom.bitbuttom8" : "L2butom.bitbuttom8_over";
		final String petButtonAction = !isPlayerTabActive ? "" : "bypass -h manage_my_buffs pet";
		
		return String.format("""
			<table width=270><tr>
			<td align=center><button value="%s" action="%s" width=130 height=19 back="%s" fore="L2butom.bitbuttom8"></td>
			<td align=center><button value="%s" action="%s" width=130 height=19 back="%s" fore="L2butom.bitbuttom8"></td>
			</tr></table><br>
			""", playerButtonValue, playerButtonAction, playerButtonBack, petButtonValue, petButtonAction, petButtonBack);
	}
	
	private String buildBuffIconGrid(Creature target, String targetType)
	{
		final List<AbstractEffect> buffs = Arrays.stream(target.getAllEffects()).filter(e -> e.getSkill() != null && !e.getSkill().isDebuff() && !e.getSkill().isToggle() && !BuffShopConfigs.NON_REMOVABLE_BUFFS.contains(e.getSkill().getId())).collect(Collectors.toList());
		if (buffs.isEmpty())
		{
			return "<tr><td colspan=7 align=center>Nenhum buff remov�vel encontrado.</td></tr>";
		}
		final StringBuilder sb = new StringBuilder("<tr>");
		for (int i = 0; i < buffs.size(); i++)
		{
			if (i > 0 && i % BUFF_REMOVAL_COLUMNS == 0)
				sb.append("</tr><tr>");
			final L2Skill skill = buffs.get(i).getSkill();
			final String bypass = String.format("bypass -h remove_buff %d %d %s", skill.getId(), skill.getLevel(), targetType);
			sb.append(String.format("<td width=40 align=center><button title=\"%s (%ds)\" action=\"%s\" width=32 height=32 back=\"Icon.skill%s\" fore=\"Icon.skill%s\"></td>", skill.getName(), buffs.get(i).getTime() / 1000, bypass, getIconSkill(skill.getId()), getIconSkill(skill.getId())));
		}
		sb.append("</tr>");
		return sb.toString();
	}
	
	/**
	 * Constr�i o rodap� da janela p�blica da loja, com o bot�o "Meus Buffs" estilizado.
	 */
	private static void buildShopFooter(StringBuilder sb)
	{
		sb.append("<br><img src=\"L2UI.SquareGray\" width=270 height=1><br>");
		
		// Adiciona o bot�o "Meus Buffs" com o mesmo estilo dos bot�es das abas
		sb.append("<button value=\"Meus Buffs\" action=\"bypass -h manage_my_buffs\" width=133 height=19 back=\"L2butom.bitbuttom8_over\" fore=\"L2butom.bitbuttom8\"></center>");
	}
	
	private static String buildSkillDescription(L2Skill skill, PrivateBuff buffEntry)
	{
		final StringBuilder sb = new StringBuilder(String.format("<font color=LEVEL>%s</font> Lv.%s<br1>Pre�o: <font color=B09878>%,d</font> Adena", skill.getName(), parseSkillLevel(skill.getLevel()), buffEntry.price()));
		if (skill.getItemConsumeId() > 0)
		{
			final Item item = ItemData.getInstance().getTemplate(skill.getItemConsumeId());
			if (item != null)
				sb.append(String.format("<br1><font color=B09878>Item:</font> %dx %s", skill.getItemConsume(), item.getName()));
		}
		return sb.toString();
	}
	
	private static void buildShopPageNavigation(StringBuilder sb, int sellerId, int activeTab, int currentPage, int totalPages)
	{
		if (totalPages <= 1)
			return;
		
		final String prevButton = (currentPage > 1) ? String.format("<button width=80 height=19 back=\"L2butom.bitbuttom8_over\" fore=\"L2butom.bitbuttom8\" action=\"bypass -h showShop %d %d %d\">", sellerId, activeTab, currentPage - 1) : "";
		final String nextButton = (currentPage < totalPages) ? String.format("<button width=80 height=19 back=\"L2butom.bitbuttom8_down\" fore=\"L2butom.bitbuttom8\" action=\"bypass -h showShop %d %d %d\">", sellerId, activeTab, currentPage + 1) : "";
		
		sb.append(String.format("<table width=270><tr><td align=left width=100>%s</td><td align=center width=70>P�g %d/%d</td><td align=right width=100>%s</td></tr></table>", prevButton, currentPage, totalPages, nextButton));
	}
	
	private static String[] buildManagementPageNavigation(String bypassCommand, int currentPage, int totalPages)
	{
		String prev = (currentPage > 1) ? String.format("<button width=64 height=32 back=\"L2UI_CH3.calculate2_sub_down\" fore=\"L2UI_CH3.calculate2_sub_over\" action=\"bypass -h %s %d\">", bypassCommand, currentPage - 1) : "";
		String next = (currentPage < totalPages) ? String.format("<button width=64 height=32 back=\"L2UI_CH3.calculate2_add_down\" fore=\"L2UI_CH3.calculate2_add_over\" action=\"bypass -h %s %d\">", bypassCommand, currentPage + 1) : "";
		String pageInfo = String.format("P�g %d/%d", currentPage, totalPages);
		
		return new String[]
		{
			prev,
			pageInfo,
			next
		};
	}
	
	/**
	 * Mostra uma janela de confirma��o rica para substituir um buff de alto n�vel.
	 * @param player O jogador que ver� a janela.
	 * @param sellerNpc O NPC vendedor.
	 * @param oldSkill O buff que o jogador j� possui.
	 * @param newSkill O buff que o jogador deseja comprar.
	 * @param adenaCost O custo em adena do novo buff.
	 * @param confirmBypass O comando de bypass para o bot�o "Sim".
	 * @param activeTab A aba da loja em que o jogador estava.
	 * @param currentPage A p�gina da loja em que o jogador estava.
	 */
	public static void showBuffReplaceConfirmation(Player player, Player sellerNpc, L2Skill oldSkill, L2Skill newSkill, long adenaCost, String confirmBypass, int activeTab, int currentPage)
	{
		// Reconstr�i o bypass de cancelamento para voltar � p�gina e aba exatas da loja.
		final String cancelBypass = String.format("bypass -h showShop %d %d %d", sellerNpc.getObjectId(), activeTab, currentPage);
		
		// --- COLETA DE DADOS PARA O HTML ---
		final String oldSkillIcon = "Icon.skill" + getIconSkill(oldSkill.getId());
		final String newSkillIcon = "Icon.skill" + getIconSkill(newSkill.getId());
		final String adenaIcon = IconTable.getInstance().getIcon(57); // ID 57 � Adena
		
		String requiredItemInfo;
		final int requiredItemId = newSkill.getItemConsumeId();
		if (requiredItemId > 0)
		{
			final Item itemTemplate = ItemData.getInstance().getTemplate(requiredItemId);
			final int requiredItemCount = newSkill.getItemConsume();
			final String itemIcon = IconTable.getInstance().getIcon(requiredItemId);
			
			// Formata a linha da tabela para o item requerido
			requiredItemInfo = String.format("<tr><td width=32><img src=\"%s\" width=32 height=32></td><td align=left>%s</td><td align=right>%s</td></tr>", itemIcon, itemTemplate.getName(), String.format("%,d", requiredItemCount));
		}
		else
		{
			// Se n�o houver item, cria uma linha vazia para manter o layout
			requiredItemInfo = "<tr><td width=32></td><td align=left>Nenhum</td><td align=right></td></tr>";
		}
		
		// --- MONTAGEM DO NOVO HTML ESTILIZADO ---
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body><center>");
		sb.append("<font color=\"LEVEL\">Confirmar Substitui��o</font><br>");
		sb.append("Isso ir� substituir seu buff atual. Deseja continuar?<br>");
		sb.append("<br>");
		
		// Tabela de Compara��o de Buffs
		sb.append("<table width=280><tr>");
		sb.append("<td width=140 align=center><u><font color=\"B09878\">Substituir Este</font></u></td>");
		sb.append("<td width=140 align=center><u><font color=\"B09878\">Por Este</font></u></td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append(String.format("<td width=140><center><img src=\"%s\" width=32 height=32><br>%s</center></td>", oldSkillIcon, oldSkill.getName()));
		sb.append(String.format("<td width=140><center><img src=\"%s\" width=32 height=32><br>%s</center></td>", newSkillIcon, newSkill.getName()));
		sb.append("</tr></table>");
		
		sb.append("<br><img src=L2UI.SquareGray width=270 height=1><br>");
		sb.append("<tr><td><center><font color=\"LEVEL\">Custos da compra:</font></center></td></tr>");
		
		// --- NOVA TABELA DE CUSTOS (ALINHADA) ---
		sb.append("<table width=180 border=0 cellpadding=0 cellspacing=2>");
		
		// Linha da Adena
		sb.append("<tr>");
		sb.append(String.format("<td width=32><img src=\"%s\" width=32 height=32></td>", adenaIcon));
		sb.append("<td width=80 align=left>Adena</td>");
		sb.append(String.format("<td width=118 align=right>%,d</td>", adenaCost));
		sb.append("</tr>");
		
		// Linha do Item Requerido
		sb.append("<tr><td><img src=L2UI.SquareGray width=40 height=1></td></tr>");
		sb.append(requiredItemInfo);
		sb.append("</table>");
		sb.append("<br><img src=L2UI.SquareGray width=270 height=1><br>");
		// Bot�es
		sb.append("<br1><center>");
		sb.append("<button value=\"Sim\" action=\"").append(confirmBypass).append("\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">");
		sb.append("<br><button value=\"N�o\" action=\"").append(cancelBypass).append("\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">");
		sb.append("</center></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(sellerNpc.getObjectId());
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	/**
	 * Exibe uma caixa de di�logo com uma mensagem para o jogador. A caixa cont�m um bot�o "OK" que retorna o jogador para a primeira p�gina da loja de skills.
	 * @param player O jogador que receber� a mensagem.
	 * @param message A mensagem a ser exibida (pode conter HTML b�sico como <br>
	 *            ).
	 */
	public void showSkillShopMessage(Player player, String message)
	{
		// O bypass para o bot�o "OK" que reabre a janela da loja na primeira p�gina.
		final String bypass = "bypass -h show_skill_shop 1";
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml("<html><head><title>Loja de Skills</title></head><body>" + "<center><br>" + message // A sua mensagem de erro ou sucesso.
			+ "<br><br><br>" + "<button value=\"OK\" action=\"" + bypass + "\" width=80 height=27 back=\"L2butom.bitbuttom8_over\" fore=\"L2butom.bitbuttom8\">" + "</center>" + "</body></html>");
		
		player.sendPacket(html);
	}
	
	/**
	 * Exibe a janela da loja de skills com sistema de pagina��o.
	 * @param player O jogador que est� vendo a loja.
	 * @param page O n�mero da p�gina a ser exibida (come�ando em 1).
	 */
	public void showSkillShopWindow(Player player, int page)
	{
		// Valida��o inicial de permiss�o.
		if (!BuffShopConfigs.BUFFSHOP_ALLOW_CLASS_SKILLSHOP.contains(player.getClassId().getId()))
		{
			player.sendMessage("Sua classe n�o tem acesso a esta loja.");
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/BuffShop/skillShop.htm");
		
		// --- 1. L�gica para o Item de Moeda Especial (sem altera��es) ---
		final int requiredItemId = BuffShopConfigs.SKILL_SHOP_REQUIRED_ITEM_ID;
		final Item itemTemplate = ItemData.getInstance().getTemplate(requiredItemId);
		
		String itemName = "Inv�lido";
		String itemIcon = "icon.etc_question_mark_i00";
		
		if (itemTemplate != null)
		{
			itemName = itemTemplate.getName();
			itemIcon = IconTable.getInstance().getIcon(requiredItemId);
		}
		else
		{
			_log.warning("BuffShop: SkillShopRequiredItemId " + requiredItemId + " � inv�lido ou n�o foi encontrado!");
		}
		
		final ItemInstance playerItems = player.getInventory().getItemByItemId(requiredItemId);
		final int itemCount = (playerItems != null) ? playerItems.getCount() : 0;
		
		html.replace("%item_icon%", itemIcon);
		html.replace("%,d", String.format("%,d", itemCount));
		html.replace("%item_name%", itemName);
		// ----------------------------------------------------
		
		// --- 2. L�gica para construir a lista de skills com PAGINA��O ---
		final StringBuilder sb = new StringBuilder();
		final List<Integer> availableSkillIds = BuffShopConfigs.SKILL_SHOP_AVAILABLE.getOrDefault(player.getClassId(), Collections.emptyList());
		
		if (availableSkillIds.isEmpty())
		{
			sb.append("<tr><td colspan=3 align=center>Nenhuma skill dispon�vel para sua classe.</td></tr>");
			html.replace("%pagination%", ""); // Limpa o placeholder de pagina��o se n�o houver skills
		}
		else
		{
			// --- L�GICA DE PAGINA��O ---
			final int totalSkills = availableSkillIds.size();
			// Calcula o n�mero total de p�ginas. Math.ceil garante que o arredondamento seja para cima.
			final int totalPages = (int) Math.ceil((double) totalSkills / SKILLS_PER_PAGE);
			
			// Garante que a p�gina solicitada seja v�lida.
			if (page < 1)
				page = 1;
			if (page > totalPages)
				page = totalPages;
			
			// Calcula o �ndice inicial e final para a sublista da p�gina atual.
			final int startIndex = (page - 1) * SKILLS_PER_PAGE;
			final int endIndex = Math.min(startIndex + SKILLS_PER_PAGE, totalSkills);
			
			// Pega apenas as skills para a p�gina atual.
			final List<Integer> skillsForPage = availableSkillIds.subList(startIndex, endIndex);
			// ---------------------------
			
			for (int skillId : skillsForPage)
			{
				final SkillPath skillPath = BuffShopConfigs.SKILL_SHOP_PATHS.get(skillId);
				if (skillPath == null)
					continue;
				
				final int currentLevel = player.getSkillLevel(skillId);
				final int nextLevel = currentLevel + 1;
				final int maxLevel = skillPath.maxLevel();
				
				String button;
				String costInfo = "";
				
				if (currentLevel >= maxLevel)
				{
					button = "<font color=00FF00>MAX</font>";
					costInfo = "N�vel M�ximo Atingido";
				}
				else
				{
					List<Cost> costs = skillPath.costsByLevel().get(nextLevel);
					if (costs != null && !costs.isEmpty())
					{
						costInfo = costs.stream().map(c -> ItemData.getInstance().getTemplate(c.itemId()).getName() + ": " + c.count()).collect(Collectors.joining(", "));
						
						boolean canAfford = costs.stream().allMatch(c -> player.getInventory().getItemByItemId(c.itemId()) != null && player.getInventory().getItemByItemId(c.itemId()).getCount() >= c.count());
						
						if (canAfford)
						{
							// O bypass para comprar continua o mesmo.
							String buyBypass = String.format("bypass -h buy_skill %d %d", skillId, nextLevel);
							button = String.format("<button value=\"Aprender\" action=\"%s\" width=75 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">", buyBypass);
						}
						else
						{
							button = "<font color=FF0000>Itens</font>";
						}
					}
					else
					{
						button = "N/A";
						costInfo = "Custo n�o definido.";
					}
				}
				
				L2Skill skillToShow = SkillTable.getInstance().getInfo(skillId, Math.max(1, currentLevel));
				
				sb.append("<tr>");
				sb.append(String.format("<td width=40><img src=\"%s\" width=32 height=32></td>", "Icon.skill" + getIconSkill(skillId)));
				sb.append(String.format("<td width=150>%s<br1><font color=LEVEL>N�vel: %d / %d</font><br1><font color=B09878>Custo: %s</font></td>", skillToShow.getName(), currentLevel, maxLevel, costInfo));
				sb.append(String.format("<td width=80 align=center>%s</td>", button));
				sb.append("<td width=80></td>");
				sb.append("</tr><br>");
				sb.append("<tr><td><img src=L2UI.SquareGray width=2 height=1></td></tr>");
				sb.append("<tr><td><img src=L2UI.SquareGray width=3 height=1></td></tr>");
			}
			
			// --- 3. Construir os controles de pagina��o (bot�es) ---
			final StringBuilder paginationHtml = new StringBuilder();
			if (totalPages > 1)
			{
				paginationHtml.append("<tr><td colspan=3><center><table width=270><tr>");
				
				// Bot�o "Anterior"
				if (page > 1)
				{
					// O bypass agora chama a pr�pria fun��o, mas com a p�gina anterior.
					String prevBypass = String.format("bypass -h show_skill_shop %d", page - 1);
					paginationHtml.append(String.format("<td width=80 align=left><button value=\"Anterior\" action=\"%s\" width=75 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>", prevBypass));
				}
				else
				{
					paginationHtml.append("<td width=80></td>"); // Espa�o em branco para alinhar
				}
				
				// Indicador "P�gina X de Y"
				paginationHtml.append(String.format("<td width=110 align=center>P�gina %d de %d</td>", page, totalPages));
				
				// Bot�o "Pr�ximo"
				if (page < totalPages)
				{
					// O bypass agora chama a pr�pria fun��o, mas com a p�gina seguinte.
					String nextBypass = String.format("bypass -h show_skill_shop %d", page + 1);
					paginationHtml.append(String.format("<td width=80 align=right><button value=\"Pr�ximo\" action=\"%s\" width=75 height=21 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>", nextBypass));
				}
				else
				{
					paginationHtml.append("<td width=80></td>"); // Espa�o em branco para alinhar
				}
				
				paginationHtml.append("</tr></table></center></td></tr>");
			}
			html.replace("%pagination%", paginationHtml.toString());
		}
		// ----------------------------------------------------
		
		html.replace("%skill_list%", sb.toString());
		player.sendPacket(html);
	}
	
	// ============================================================================================
	// 4. M�TODOS UTILIT�RIOS
	// ============================================================================================
	
	private boolean checkBuffCondition(final L2Skill skill)
	{
		if (skill.isPassive() || skill.isToggle() || BuffShopConfigs.BUFFSHOP_FORBIDDEN_SKILL.contains(skill.getId()))
			return false;
		if (BuffShopConfigs.BUFFSHOP_ALLOWED_SELF_SKILLS.contains(skill.getId()))
			return true;
		return skill.getSkillType() == SkillType.BUFF && !skill.isOffensive() && !skill.isDebuff() && !Set.of(SkillTargetType.SELF, SkillTargetType.CORPSE_MOB, SkillTargetType.AURA, SkillTargetType.AREA, SkillTargetType.AREA_CORPSE_MOB, SkillTargetType.HOLY).contains(skill.getTargetType());
	}
	
	private static String parseSkillLevel(final int level)
	{
		return (level > 100) ? "<font color=FFFF00>+" + (level % 100) + "</font>" : String.valueOf(level);
	}
	
	private static String getIconSkill(int skillId)
	{
		if (skillId >= 4699 && skillId <= 4700)
			return "1331";
		if (skillId >= 4702 && skillId <= 4703)
			return "1332";
		return String.format("%04d", skillId);
	}
	
	public void sendHtml(Player player, String html)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(html);
		player.sendPacket(msg);
	}
}