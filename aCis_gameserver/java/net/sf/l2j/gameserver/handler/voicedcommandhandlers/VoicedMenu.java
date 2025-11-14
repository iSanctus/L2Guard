package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * @author evert
 */
public class VoicedMenu implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"menu",
	
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String params)
	{
		if (command.startsWith("menu"))
		{
			player.sendMessage("comando indisponivel menu");
			player.sendMessage("dfss");
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}
