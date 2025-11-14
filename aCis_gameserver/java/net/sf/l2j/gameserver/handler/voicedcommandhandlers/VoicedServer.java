package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * @author evert
 */
public class VoicedServer implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"server"
	
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String params)
	{
		if (command.startsWith("server"))
		{
			player.sendMessage("bem vindo ao projeto l2 targton");
			player.sendMessage("caso queira alguma information");
			player.sendMessage("contato 69 993097570 ex");
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
