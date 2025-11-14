package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastDDMagicPhysicalSpecial;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCastDDMagicPhysicalSpecialBuffAggressive extends WarriorCastDDMagicPhysicalSpecial
{
	public WarriorCastDDMagicPhysicalSpecialBuffAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastDDMagicPhysicalSpecial");
	}
	
	public WarriorCastDDMagicPhysicalSpecialBuffAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21384,
		21643
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		final L2Skill buff = getNpcSkillByType(npc, NpcSkillType.BUFF);
		npc.getAI().addCastDesire(npc, buff, 1000000);
		
		super.onCreated(npc);
	}
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (npc.getAI().getCurrentIntention().getType() != IntentionType.ATTACK && npc.getAI().getCurrentIntention().getType() != IntentionType.CAST)
		{
			final L2Skill buff = getNpcSkillByType(npc, NpcSkillType.BUFF);
			if (getAbnormalLevel(creature, buff) <= 0)
				npc.getAI().addCastDesire(npc, buff, 1000000);
		}
		
		if (!(creature instanceof Playable))
			return;
		
		tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			if (npc.getAI().getCurrentIntention().getType() != IntentionType.ATTACK && npc.getAI().getCurrentIntention().getType() != IntentionType.CAST)
			{
				final L2Skill buff = getNpcSkillByType(npc, NpcSkillType.BUFF);
				if (getAbnormalLevel(npc, buff) <= 0)
					npc.getAI().addCastDesire(npc, buff, 1000000);
			}
		}
		super.onAttacked(npc, attacker, damage, skill);
	}
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable && called.getAI().getLifeTime() > 7)
		{
			if (called.getAI().getCurrentIntention().getType() != IntentionType.ATTACK && called.getAI().getCurrentIntention().getType() != IntentionType.CAST)
			{
				final L2Skill buff = getNpcSkillByType(called, NpcSkillType.BUFF);
				if (getAbnormalLevel(caller, buff) <= 0)
					called.getAI().addCastDesire(caller, buff, 1000000);
			}
		}
		super.onClanAttacked(caller, called, attacker, damage, skill);
	}
}