package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.PartyPrivateWarrior;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class PartyPrivateCastHealCurse extends PartyPrivateCastHeal
{
	public PartyPrivateCastHealCurse()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/PartyPrivateWarrior");
	}
	
	public PartyPrivateCastHealCurse(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21060,
		20962,
		20968,
		20988,
		20940,
		20768,
		20748,
		21077,
		21083,
		21092,
		22130
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			Creature mostHated = npc.getAI().getAggroList().getMostHatedCreature();
			if (mostHated != null)
			{
				L2Skill debuff = getNpcSkillByType(npc, NpcSkillType.DEBUFF);
				
				if (Rnd.get(100) < 33 && getAbnormalLevel(attacker, debuff) <= 0 && mostHated == attacker)
				{
					npc.getAI().addCastDesire(attacker, debuff, 1000000);
				}
			}
		}
		super.onAttacked(npc, attacker, damage, skill);
	}
	
	@Override
	public void onPartyAttacked(Npc caller, Npc called, Creature target, int damage)
	{
		if (target instanceof Playable)
		{
			Creature mostHated = called.getAI().getAggroList().getMostHatedCreature();
			if (mostHated != null)
			{
				L2Skill debuff = getNpcSkillByType(called, NpcSkillType.DEBUFF);
				
				if (Rnd.get(100) < 33 && getAbnormalLevel(target, debuff) <= 0 && mostHated == target)
				{
					called.getAI().addCastDesire(target, debuff, 1000000);
				}
			}
		}
		super.onPartyAttacked(caller, called, target, damage);
	}
}
