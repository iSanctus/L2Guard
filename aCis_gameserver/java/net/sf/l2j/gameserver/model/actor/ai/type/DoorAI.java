package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Door;

public class DoorAI extends CreatureAI<Door>
{
	public DoorAI(Door door)
	{
		super(door);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		// Do nothing.
	}
	
	@Override
	protected void onEvtFinishedAttack()
	{
		// Do nothing.
	}
	
	@Override
	protected void onEvtArrived()
	{
		// Do nothing.
	}
	
	@Override
	protected void onEvtArrivedBlocked()
	{
		// Do nothing.
	}
	
	@Override
	protected void onEvtDead()
	{
		// Do nothing.
	}
}