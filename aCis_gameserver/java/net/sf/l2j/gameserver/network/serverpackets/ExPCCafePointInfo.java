package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class ExPCCafePointInfo extends L2GameServerPacket
{
	private final Player _player;
	private final int _addPoint;
	private final int _periodType;
	private final int _remainTime;
	private final int _pointType;

	public ExPCCafePointInfo(Player player, int modify, boolean add, int hour, boolean dbl)
	{
		_player = player;
		_addPoint = modify;
		_remainTime = hour;

		if (add)
		{
			_periodType = 1;
			_pointType = dbl ? 0 : 1;
		}
		else
		{
			_periodType = 2;
			_pointType = 2;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(_player.getPcBangScore());
		writeD(_addPoint);
		writeC(_periodType);
		writeD(_remainTime);
		writeC(_pointType);
	}
}