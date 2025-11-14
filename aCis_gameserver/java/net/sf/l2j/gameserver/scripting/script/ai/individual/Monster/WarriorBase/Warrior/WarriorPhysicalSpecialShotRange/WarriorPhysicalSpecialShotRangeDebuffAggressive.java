package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorPhysicalSpecialShotRange;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorPhysicalSpecialShotRangeDebuffAggressive extends WarriorPhysicalSpecialShotRangeAggressive
{
	public WarriorPhysicalSpecialShotRangeDebuffAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorPhysicalSpecialShotRange");
	}
	
	public WarriorPhysicalSpecialShotRangeDebuffAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22054
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (npc.getStatus().getHpRatio() < 0.33 && Rnd.get(100) < 60)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DEBUFF1), 1000000);
		else if (npc.getStatus().getHpRatio() < 0.33)
		{
			if (Rnd.get(100) < 50)
			{
				if (Rnd.get(100) < 50)
					npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DEBUFF2), 1000000);
				else
					npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DEBUFF3), 1000000);
			}
			else if (Rnd.get(100) < 50)
				npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DEBUFF4), 1000000);
			else
				npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DEBUFF5), 1000000);
		}
		
		super.onAttacked(npc, attacker, damage, skill);
	}
}
