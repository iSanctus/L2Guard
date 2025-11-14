package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastingCurse;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Agit02VampireAgitParty extends Agit02DoomKnightAgitParty
{
	public Agit02VampireAgitParty()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastingCurse");
	}
	
	public Agit02VampireAgitParty(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35647
	};
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (called.getAI().getLifeTime() > 8 && attacker instanceof Playable)
		{
			final L2Skill debuff = getNpcSkillByType(called, NpcSkillType.DEBUFF);
			if (Rnd.get(100) < 10 && getAbnormalLevel(caller, debuff) <= 0)
				called.getAI().addCastDesire(attacker, debuff, 1000000);
		}
		
		if (called._flag == caller._flag && attacker instanceof Playable)
			called.getAI().addAttackDesire(attacker, (((damage * 1.0) / called.getStatus().getMaxHp()) / 0.05) * 100);
	}
	
	@Override
	public void onPartyAttacked(Npc caller, Npc called, Creature target, int damage)
	{
		// Do nothing
	}
}
