package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorPhysicalSpecial;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.NpcStringId;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorPhysicalSpecialSpoilAggressive extends WarriorPhysicalSpecialAggressive
{
	public WarriorPhysicalSpecialSpoilAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorPhysicalSpecial");
	}
	
	public WarriorPhysicalSpecialSpoilAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22017,
		22024
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		npc._i_ai1 = 0;
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (npc._i_ai1 == 0)
		{
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.EFFECT_SKILL), 1000000);
			npc._i_ai1 = 1;
		}
		
		if (Rnd.get(100) < 33)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.PHYSICAL_SPECIAL), 1000000);
		
		super.onAttacked(npc, attacker, damage, skill);
	}
	
	@Override
	public void onAttackFinished(Npc npc, Creature target)
	{
		if (target.isDead() && target instanceof Player)
			npc.broadcastNpcSay(NpcStringId.ID_1010584);
	}
	
	@Override
	public void onUseSkillFinished(Npc npc, Creature creature, L2Skill skill, boolean success)
	{
		if (skill == getNpcSkillByType(npc, NpcSkillType.EFFECT_SKILL) && success)
		{
			final int i0 = Rnd.get(100);
			if (i0 < 30)
				npc.broadcastNpcSay(NpcStringId.ID_10068);
			else if (i0 < 60)
				npc.broadcastNpcSay(NpcStringId.ID_10069);
			else
				npc.broadcastNpcSay(NpcStringId.ID_10070);
		}
	}
}
