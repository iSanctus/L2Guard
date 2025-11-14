package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorAggressive.WarriorAggressive;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorUseBowNotFleeAggressive extends WarriorAggressive
{
	public WarriorUseBowNotFleeAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior");
	}
	
	public WarriorUseBowNotFleeAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22132
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (npc.getStatus().getHpRatio() < 0.33)
			npc.getAI().addCastDesire(npc, getNpcSkillByType(npc, NpcSkillType.MAGIC_HEAL), 1000000);
		
		if (Rnd.get(100) < 33 && npc.getAI().getTopDesireTarget() == attacker)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.LONG_RANGE_PHYSICAL_SPECIAL), 1000000);
		
		super.onAttacked(npc, attacker, damage, skill);
	}
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (caller.getStatus().getHpRatio() < 0.33)
			called.getAI().addCastDesire(caller, getNpcSkillByType(called, NpcSkillType.MAGIC_HEAL), 1000000);
		
		super.onClanAttacked(caller, called, attacker, damage, skill);
	}
}
