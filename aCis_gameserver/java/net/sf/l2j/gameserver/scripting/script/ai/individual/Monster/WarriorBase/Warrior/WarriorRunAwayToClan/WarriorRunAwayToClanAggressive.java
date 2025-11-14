package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorRunAwayToClan;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;

public class WarriorRunAwayToClanAggressive extends WarriorRunAwayToClan
{
	public WarriorRunAwayToClanAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorRunAwayToClan");
	}
	
	public WarriorRunAwayToClanAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		20211,
		20438,
		20061,
		20497,
		20495
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
			return;
		
		tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
}