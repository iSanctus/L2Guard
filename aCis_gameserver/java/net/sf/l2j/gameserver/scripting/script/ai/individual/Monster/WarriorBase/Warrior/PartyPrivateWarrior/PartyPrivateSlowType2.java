package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.PartyPrivateWarrior;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class PartyPrivateSlowType2 extends PartyPrivateSlowTypeMagic
{
	public PartyPrivateSlowType2()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/PartyPrivateWarrior");
	}
	
	public PartyPrivateSlowType2(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22091
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (npc.distance2D(attacker) > 200 && Rnd.get(100) < 70)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DD_MAGIC_SLOW), 1000000);
		
		super.onAttacked(npc, attacker, damage, skill);
	}
}
