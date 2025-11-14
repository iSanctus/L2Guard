package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastDDMagicPhysicalSpecial;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.Warrior;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCastDDMagicPhysicalSpecial extends Warrior
{
	public WarriorCastDDMagicPhysicalSpecial()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastDDMagicPhysicalSpecial");
	}
	
	public WarriorCastDDMagicPhysicalSpecial(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22046,
		22049
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			Creature mostHated = npc.getAI().getAggroList().getMostHatedCreature();
			
			if (mostHated != null)
			{
				if (npc.distance2D(attacker) > 200 && mostHated == attacker)
				{
					L2Skill longRangeDD = getNpcSkillByType(npc, NpcSkillType.W_LONG_RANGE_DD_MAGIC);
					
					npc.getAI().addCastDesire(attacker, longRangeDD, 1000000);
				}
				if (Rnd.get(100) < 33 && mostHated != attacker && npc.distance2D(attacker) < 200)
				{
					L2Skill physicalSpecialRange = getNpcSkillByType(npc, NpcSkillType.PHYSICAL_SPECIAL_RANGE);
					
					npc.getAI().addCastDesire(attacker, physicalSpecialRange, 1000000);
				}
			}
		}
		
		super.onAttacked(npc, attacker, damage, skill);
	}
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (called.getAI().getLifeTime() > 7 && attacker instanceof Playable)
		{
			Creature mostHated = called.getAI().getAggroList().getMostHatedCreature();
			
			if (mostHated != null)
			{
				if (called.distance2D(attacker) < 200 && Rnd.get(100) < 33 && mostHated != attacker)
				{
					L2Skill physicalSpecialRange = getNpcSkillByType(called, NpcSkillType.PHYSICAL_SPECIAL_RANGE);
					
					called.getAI().addCastDesire(attacker, physicalSpecialRange, 1000000);
				}
			}
		}
		
		super.onClanAttacked(caller, called, attacker, damage, skill);
	}
	
	@Override
	public void onUseSkillFinished(Npc npc, Creature creature, L2Skill skill, boolean success)
	{
		Creature mostHated = npc.getAI().getAggroList().getMostHatedCreature();
		
		if (mostHated != null)
		{
			if (npc.distance2D(mostHated) > 200 && mostHated instanceof Player)
			{
				L2Skill longRangeDD = getNpcSkillByType(npc, NpcSkillType.W_LONG_RANGE_DD_MAGIC);
				
				npc.getAI().addCastDesire(mostHated, longRangeDD, 1000000);
			}
		}
		
		super.onUseSkillFinished(npc, creature, skill, success);
	}
}
