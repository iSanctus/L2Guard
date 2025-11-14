package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.RoyalRushRoomBossBasic;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class RoyalRushRoomBoss5 extends RoyalRushRoomBossBasic
{
	public RoyalRushRoomBoss5()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/RoyalRushRoomBossBasic");
	}
	
	public RoyalRushRoomBoss5(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		18122,
		18125,
		18128,
		18131,
		18175,
		18178,
		18181,
		18184
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			final Creature topDesireTarget = npc.getAI().getTopDesireTarget();
			if (topDesireTarget != null)
			{
				if (Rnd.get(100) < 33 && topDesireTarget != attacker)
					npc.getAI().addCastDesire(npc, getNpcSkillByType(npc, NpcSkillType.SELF_RANGE_DD_MAGIC1), 1000000);
				
				if (Rnd.get(100) < 33 && topDesireTarget == attacker)
					npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DEBUFF), 1000000);
				
				if (Rnd.get(100) < 33 && topDesireTarget == attacker)
					npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DD_MAGIC), 1000000);
			}
		}
		
		super.onAttacked(npc, attacker, damage, skill);
	}
}
