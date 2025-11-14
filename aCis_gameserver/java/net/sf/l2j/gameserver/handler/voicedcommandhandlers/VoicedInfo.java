package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * @author evert
 */
public class VoicedInfo implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"info",
	
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String params)
	{
		if (command.startsWith("info"))
		{
			player.sendMessage("comando info server " + Config.RATE_QUEST_REWARD_ADENA +"x");
			player.sendMessage("10x SP/XP/ADENA");
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
