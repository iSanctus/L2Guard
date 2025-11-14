package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCast3SkillsMagical3;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCast3SkillsMagical3Aggressive extends WarriorCast3SkillsMagical3
{
	public WarriorCast3SkillsMagical3Aggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCast3SkillsMagical3");
	}
	
	public WarriorCast3SkillsMagical3Aggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		20821,
		20628,
		20856,
		18007
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
		{
			super.onSeeCreature(npc, creature);
			return;
		}
		
		if (npc.getAI().getLifeTime() > 7 && npc.getAI().getCurrentIntention().getType() == IntentionType.WANDER && npc.isInMyTerritory())
		{
			final L2Skill debuff = getNpcSkillByType(npc, NpcSkillType.DEBUFF);
			if (Rnd.get(100) < 33 && getAbnormalLevel(creature, debuff) <= 0)
				npc.getAI().addCastDesire(creature, debuff, 1000000);
			
			if (npc.distance2D(creature) > 100 && Rnd.get(100) < 33)
			{
				final L2Skill DDMagic = getNpcSkillByType(npc, NpcSkillType.DD_MAGIC);
				npc.getAI().addCastDesire(creature, DDMagic, 1000000);
			}
		}
		
		tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
}