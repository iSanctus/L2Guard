package net.sf.l2j.gameserver.model.boat;

import java.util.List;

import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public record ScheduledBoatMessages(int delay, List<L2GameServerPacket> messages)
{
}