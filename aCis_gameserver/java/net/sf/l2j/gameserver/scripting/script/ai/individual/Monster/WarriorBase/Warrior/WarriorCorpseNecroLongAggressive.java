package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCorpseNecroLongAggressive extends Warrior
{
	public WarriorCorpseNecroLongAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior");
	}
	
	public WarriorCorpseNecroLongAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21574,
		21567
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
		{
			super.onSeeCreature(npc, creature);
			return;
		}
		
		if (npc.getAI().getCurrentIntention().getType() != IntentionType.ATTACK)
		{
			final double dist = npc.distance2D(creature);
			
			if (npc.getAI().getLifeTime() > 7 && dist > 100 && Rnd.get(100) < 33 && npc.isInMyTerritory())
				npc.getAI().addCastDesire(creature, getNpcSkillByType(npc, NpcSkillType.PHYSICAL_SPECIAL), 1000000);
			
			if (creature.isDead() && dist < 100 && Rnd.get(100) < 33)
			{
				final Creature mostHated = npc.getAI().getAggroList().getMostHatedCreature();
				if (mostHated != null)
					npc.getAI().addCastDesire(creature, getNpcSkillByType(npc, NpcSkillType.DD_MAGIC2), 1000000);
			}
		}
		super.onSeeCreature(npc, creature);
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			final Creature mostHated = npc.getAI().getAggroList().getMostHatedCreature();
			if (mostHated == attacker)
			{
				if (npc.distance2D(attacker) > 100)
					npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.PHYSICAL_SPECIAL), 1000000);
				
				if (Rnd.get(100) < 33 && npc.getStatus().getHpRatio() < 0.4)
					npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DD_MAGIC1), 1000000);
			}
		}
		super.onAttacked(npc, attacker, damage, skill);
	}
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable && called.getAI().getLifeTime() > 7 && called.getAI().getCurrentIntention().getType() != IntentionType.ATTACK && called.distance2D(attacker) > 100)
			called.getAI().addCastDesire(attacker, getNpcSkillByType(called, NpcSkillType.PHYSICAL_SPECIAL), 1000000);
		
		super.onClanAttacked(caller, called, attacker, damage, skill);
	}
}