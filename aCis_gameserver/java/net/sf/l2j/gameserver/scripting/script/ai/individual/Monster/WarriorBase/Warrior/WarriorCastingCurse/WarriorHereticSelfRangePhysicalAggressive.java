package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastingCurse;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;

public class WarriorHereticSelfRangePhysicalAggressive extends WarriorHereticSelfRangePhysical
{
	public WarriorHereticSelfRangePhysicalAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastingCurse");
	}
	
	public WarriorHereticSelfRangePhysicalAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22141
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (creature instanceof Playable)
			tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
}
