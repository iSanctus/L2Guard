package net.sf.l2j.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.handler.voicedcommandhandlers.BuffManagerVCmd;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedInfo;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedMenu;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedServer;

/**
 * @author evert
 */

public class VoicedCommandHandler
{
	private final Map<String, IVoicedCommandHandler> _datatable = new HashMap<>();
	
	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected VoicedCommandHandler()
	{
		// Aqui voce registra os comandos customizados
		registerHandler(new VoicedMenu());
		registerHandler(new VoicedServer());
		registerHandler(new VoicedInfo());
		registerHandler(new BuffManagerVCmd());
		
	}
	
	public void registerHandler(IVoicedCommandHandler handler)
	{
		for (String id : handler.getVoicedCommandList())
			_datatable.put(id.toLowerCase(), handler);
	}
	
	public IVoicedCommandHandler getHandler(String voicedCommand)
	{
		String command = voicedCommand;
		
		if (voicedCommand.contains(" "))
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		
		return _datatable.get(command.toLowerCase()); // busca em lowercase
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	}
}