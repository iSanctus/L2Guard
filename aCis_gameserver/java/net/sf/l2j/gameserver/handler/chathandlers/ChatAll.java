package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.enums.FloodProtector;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatAll implements IChatHandler
{
	private static final SayType[] COMMAND_IDS =
	{
		SayType.ALL
	};
	
	@Override
	public void handleChat(SayType type, Player player, String target, String text)
	{
		if (!player.getClient().performAction(FloodProtector.GLOBAL_CHAT))
			return;
		
		if (text.startsWith("."))
		{
			String command = text.substring(1).toLowerCase();
			final IVoicedCommandHandler voiced = VoicedCommandHandler.getInstance().getHandler(text.substring(1).toLowerCase());
			if (voiced != null)
			{
				voiced.useVoicedCommand(command, player, text);
				return;
			}
		}
		final CreatureSay cs = new CreatureSay(player, type, text);
		
		player.sendPacket(cs);
		player.forEachKnownTypeInRadius(Player.class, 1250, p -> p.sendPacket(cs));
	}
	
	@Override
	public SayType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}