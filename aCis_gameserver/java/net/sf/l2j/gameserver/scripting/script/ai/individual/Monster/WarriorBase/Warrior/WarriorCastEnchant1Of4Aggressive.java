package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCastEnchant1Of4Aggressive extends WarriorCastEnchant1Of4
{
	public WarriorCastEnchant1Of4Aggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior");
	}
	
	public WarriorCastEnchant1Of4Aggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21426,
		20554,
		21304,
		20588
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (npc.getAI().getLifeTime() > 7 && npc.isInMyTerritory())
		{
			if (npc._i_ai1 == 0 && Rnd.get(100) < 33 && npc.getStatus().getHpRatio() > 0.5)
			{
				final L2Skill selfBuff = getNpcSkillByType(npc, BUFFS[npc._i_ai0]);
				npc.getAI().addCastDesire(npc, selfBuff, 1000000);
			}
			npc._i_ai1 = 1;
		}
		
		if (!(creature instanceof Playable))
			return;
		
		tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
}