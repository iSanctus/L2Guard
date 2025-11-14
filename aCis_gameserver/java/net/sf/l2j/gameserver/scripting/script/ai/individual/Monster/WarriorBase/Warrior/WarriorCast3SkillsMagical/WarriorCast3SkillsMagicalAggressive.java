package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCast3SkillsMagical;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCast3SkillsMagicalAggressive extends WarriorCast3SkillsMagical
{
	public WarriorCast3SkillsMagicalAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCast3SkillsMagical");
	}
	
	public WarriorCast3SkillsMagicalAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		20822,
		20627,
		20828,
		20810,
		20847,
		21635,
		21636,
		21165,
		21637,
		18006
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
			return;
		
		if (npc.getAI().getLifeTime() > 7 && npc.getAI().getCurrentIntention().getType() != IntentionType.ATTACK && npc.isInMyTerritory())
		{
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