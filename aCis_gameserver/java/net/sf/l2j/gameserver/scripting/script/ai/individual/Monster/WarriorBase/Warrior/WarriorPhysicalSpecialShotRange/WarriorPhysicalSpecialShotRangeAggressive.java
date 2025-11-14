package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorPhysicalSpecialShotRange;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;

public class WarriorPhysicalSpecialShotRangeAggressive extends WarriorPhysicalSpecialShotRange
{
	public WarriorPhysicalSpecialShotRangeAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorPhysicalSpecialShotRange");
	}
	
	public WarriorPhysicalSpecialShotRangeAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22053
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
			return;
		
		tryToAttack(npc, creature);
		
		if (npc.distance2D(creature) > 200)
			npc.getAI().addCastDesire(creature, getNpcSkillByType(npc, NpcSkillType.LONG_RANGE_PHYSICAL_SPECIAL), 1000000);
		
		startQuestTimer("1002", npc, null, 5000);
		
		super.onSeeCreature(npc, creature);
	}
}