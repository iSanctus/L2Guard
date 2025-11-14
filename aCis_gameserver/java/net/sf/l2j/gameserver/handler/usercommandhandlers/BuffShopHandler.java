package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.util.sellBuffEngine.BuffShopUIManager;
/*L2jbrasil - Dhousefe - 19/07/2025 */
public class BuffShopHandler implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		203
	};
	
	@Override
	public void useUserCommand(int id, Player player)
	{
		BuffShopUIManager.getInstance().showIndexWindow(player, null);
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}