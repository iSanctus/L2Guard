package net.sf.l2j.gameserver.model.entity.events;

import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class PCBangPoint implements Runnable
{
	private static final Logger _log = Logger.getLogger(PCBangPoint.class.getName());
	private static PCBangPoint _instance;

	public static PCBangPoint getInstance()
	{
		if (_instance == null)
			_instance = new PCBangPoint();

		return _instance;
	}

	private PCBangPoint()
	{
		_log.info("PC Bang Point Event initialized.");
	}

	@Override
	public void run()
	{
		for (Player activeChar : World.getInstance().getPlayers())
		{
			if (activeChar.getStatus().getLevel() < Config.PCB_MIN_LEVEL)
				continue;

			int score = Rnd.get(Config.PCB_POINT_MIN, Config.PCB_POINT_MAX);

			boolean isDouble = (Rnd.get(100) <= Config.PCB_CHANCE_DUAL_POINT);
			if (isDouble)
				score *= 2;

			activeChar.addPcBangScore(score);

			SystemMessage sm = isDouble ? SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE) : SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT);
			sm.addNumber(score);
			activeChar.sendPacket(sm);

			activeChar.updatePcBangWnd(score, true, isDouble);
		}
	}
}