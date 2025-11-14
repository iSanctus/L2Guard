package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;
/**
 * @author evert
 *
 */

public interface IVoicedCommandHandler
{
	public boolean useVoicedCommand(String command, Player activeChar, String params);
	
	public String[] getVoicedCommandList();
 }
