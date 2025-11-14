package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.MoveType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.ServerObjectInfo;

/**
 * A zone extending {@link ZoneType}, used for the water behavior. {@link Player}s can drown if they stay too long below water line.
 */
public class WaterZone extends ZoneType
{
	public WaterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		creature.setInsideZone(ZoneId.WATER, true);
		creature.getMove().addMoveType(MoveType.SWIM);
		
		if (creature instanceof Player player)
			player.broadcastUserInfo();
		else if (creature instanceof Npc npc)
		{
			npc.forEachKnownType(Player.class, player ->
			{
				if (npc.getStatus().getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo(npc, player));
				else
					player.sendPacket(new NpcInfo(npc, player));
			});
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.WATER, false);
		creature.getMove().removeMoveType(MoveType.SWIM);
		
		if (creature instanceof Player player)
			player.broadcastUserInfo();
		else if (creature instanceof Npc npc)
		{
			npc.forEachKnownType(Player.class, player ->
			{
				if (npc.getStatus().getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo(npc, player));
				else
					player.sendPacket(new NpcInfo(npc, player));
			});
		}
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}