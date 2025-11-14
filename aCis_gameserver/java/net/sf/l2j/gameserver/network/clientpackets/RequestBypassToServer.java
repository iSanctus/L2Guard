package net.sf.l2j.gameserver.network.clientpackets;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.CommunityBoard;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.enums.FloodProtector;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.handler.itemhandlers.ActiveItemSpecial;
import net.sf.l2j.gameserver.model.L2AutoFarmTask;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.OlympiadManagerNpc;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final Logger GMAUDIT_LOG = Logger.getLogger("gmaudit");
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	// Adicione este metodo dentro da classe RequestBypassToServer
	private boolean isBuffShopManagerCommand(String fullCommand)
	{
		if (fullCommand == null || fullCommand.isEmpty())
		{
			return false;
		}
		
		String commandPart = fullCommand;
		int spaceIdx = fullCommand.indexOf(" ");
		if (spaceIdx != -1)
		{
			commandPart = fullCommand.substring(0, spaceIdx);
		}
		
		switch (commandPart)
		{
			case "index":
			case "setprice":
			case "settitle":
			case "setbuffs":
			case "close":
			case "setshop":
			case "list":
			case "add":
			case "del":
			case "startshop":
			case "stopshop":
			case "showShop":
			case "cast":
			case "remove_buff":
			case "remove_buffatt":
			case "manage_my_buffs":
			case "cast_confirm":
			case "shopskill":
			case "buy_skill":
			case "show_skill_shop":
				return true;
			default:
				return false;
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_command == null || _command.isEmpty())
			return;
		
		if (!getClient().performAction(FloodProtector.SERVER_BYPASS))
			return;
		
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		try
		{
			if (_command.startsWith("admin_"))
				handleAdminCommand(player);
			else if (_command.startsWith("player_help "))
				handlePlayerHelp(player);
			else if (_command.startsWith("manor_menu_select?"))
				handleManor(player);
			else if (_command.startsWith("bbs_") || _command.startsWith("_bbs") || _command.startsWith("_friend") || _command.startsWith("_mail") || _command.startsWith("_block"))
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			else if (_command.startsWith("Quest "))
				handleQuest(player);
			else if (_command.startsWith("_match") || _command.startsWith("_diary"))
				handleHero(player);
			else if (_command.startsWith("arenachange"))
				handleOlympiad(player);
			else if (_command.startsWith("name_change "))
			{
				String newName = _command.substring(12).trim();
				ActiveItemSpecial.processNameChange(player, newName, null);
			}
			else if (_command.startsWith("class_index_select"))
				handleClassChange(player);
			// === NOVO: AutoFarm ===
			else if (_command.startsWith("farm"))
			{
				L2AutoFarmTask bot = player.getFarm();
				final StringTokenizer tokenizer = new StringTokenizer(_command);
				tokenizer.nextToken(); // pula o "farm"
				if (!tokenizer.hasMoreTokens())
				{
					player.sendMessage("Use farm on/off.");
					return;
				}
				final String param = tokenizer.nextToken();
				
				switch (param.toLowerCase())
				{
					case "on":
						if (!bot.running())
						{
							bot.start();
							player.sendMessage("AutoFarm ativado.");
						}
						break;

					case "off":
						if (bot.running())
						{
							bot.stop();
							player.sendMessage("AutoFarm desativado.");
						}
						break;

					default:
						player.sendMessage("Use farm on/off.");
						break;
				}
			}
			// === FIM AutoFarm ===
			else if (_command.startsWith("npc_"))
				handleNpcBypass(player);
		}
		catch (Exception e)
		{
			player.sendMessage("An error occurred processing your command.");
			e.printStackTrace();
		}
	}
	
	private void handleAdminCommand(Player player)
	{
		String command = _command.split(" ")[0];
		final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
		if (ach == null)
		{
			if (player.isGM())
				player.sendMessage("The command " + command.substring(6) + " doesn't exist.");
			return;
		}
		
		if (!AdminData.getInstance().hasAccess(command, player.getAccessLevel()))
		{
			player.sendMessage("You don't have the access rights to use this command.");
			return;
		}
		
		if (Config.GMAUDIT)
			GMAUDIT_LOG.info(player.getName() + " [" + player.getObjectId() + "] used '" + _command + "' command on: " + ((player.getTarget() != null) ? player.getTarget().getName() : "none"));
		
		ach.useAdminCommand(_command, player);
	}
	
	private void handlePlayerHelp(Player player)
	{
		final String path = _command.substring(12);
		if (path.contains(".."))
			return;
		
		final StringTokenizer st = new StringTokenizer(path);
		final String[] cmd = st.nextToken().split("#");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/help/" + cmd[0]);
		html.disableValidation();
		player.sendPacket(html);
	}
	
	private void handleNpcBypass(Player player)
	{
		if (!player.validateBypass(_command))
			return;
		
		int endOfId = _command.indexOf('_', 5);
		String id = endOfId > 0 ? _command.substring(4, endOfId) : _command.substring(4);
		
		try
		{
			final WorldObject object = World.getInstance().getObject(Integer.parseInt(id));
			if (object instanceof Npc npc && endOfId > 0 && player.getAI().canDoInteract(npc))
				npc.onBypassFeedback(player, _command.substring(endOfId + 1));
			
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		catch (NumberFormatException ignored)
		{
		}
	}
	
	private void handleManor(Player player)
	{
		WorldObject object = player.getTarget();
		if (object instanceof Npc targetNpc)
			targetNpc.onBypassFeedback(player, _command);
	}
	
	private void handleQuest(Player player)
	{
		if (!player.validateBypass(_command))
			return;
		
		String[] str = _command.substring(6).trim().split(" ", 2);
		if (str.length == 1)
			player.getQuestList().processQuestEvent(str[0], "");
		else
			player.getQuestList().processQuestEvent(str[0], str[1]);
	}
	
	private void handleHero(Player player)
	{
		String params = _command.substring(_command.indexOf("?") + 1);
		StringTokenizer st = new StringTokenizer(params, "&");
		int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
		int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
		int heroid = HeroManager.getInstance().getHeroByClass(heroclass);
		
		if (_command.startsWith("_match") && heroid > 0)
			HeroManager.getInstance().showHeroFights(player, heroclass, heroid, heropage);
		else if (_command.startsWith("_diary") && heroid > 0)
			HeroManager.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
	}
	
	private void handleOlympiad(Player player)
	{
		final boolean isManager = player.getCurrentFolk() instanceof OlympiadManagerNpc;
		
		if (!isManager && (!player.isInObserverMode() || player.isInOlympiadMode() || player.getOlympiadGameId() < 0))
			return;
		
		if (OlympiadManager.getInstance().isRegisteredInComp(player))
		{
			player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
			return;
		}
		
		final int arenaId = Integer.parseInt(_command.substring(12).trim());
		player.enterOlympiadObserverMode(arenaId);
	}
	
	private void handleClassChange(Player player)
	{
		String[] cmd = _command.split(" ");
		if (cmd.length < 2)
		{
			player.sendMessage("Invalid class selection.");
			return;
		}
		
		String classParam = cmd[1];
		ItemInstance item = player.getInventory().getItemByItemId(ActiveItemSpecial.CHANGE_CLASS);
		if (item == null)
		{
			player.sendMessage("You don't have the required item.");
			return;
		}
		
		try
		{
			int newClassId = Integer.parseInt(classParam);
			ActiveItemSpecial.applyClassChange(player, newClassId, item);
		}
		catch (NumberFormatException e)
		{
			int newClassId = getClassIdByName(classParam);
			if (newClassId == -1)
			{
				player.sendMessage("Invalid class name.");
				return;
			}
			ActiveItemSpecial.applyClassChange(player, newClassId, item);
		}
	}
	
	private int getClassIdByName(String className)
	{
		for (ClassId cid : ClassId.values())
		{
			if (cid.name().equalsIgnoreCase(className))
				return cid.getId();
		}
		return -1;
	}
}
