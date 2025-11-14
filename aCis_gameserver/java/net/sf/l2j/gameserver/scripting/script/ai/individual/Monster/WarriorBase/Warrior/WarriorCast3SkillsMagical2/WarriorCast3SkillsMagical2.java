package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCast3SkillsMagical2;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.Warrior;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCast3SkillsMagical2 extends Warrior
{
	public WarriorCast3SkillsMagical2()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCast3SkillsMagical2");
	}
	
	public WarriorCast3SkillsMagical2(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		20411,
		20416,
		20505,
		20618,
		20862,
		27184,
		21396,
		20202,
		21020,
		21108,
		20154,
		20223,
		20225,
		20799,
		20109,
		20609,
		20415,
		21595
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		
		if (attacker instanceof Playable)
		{
			if (npc.distance2D(attacker) > 100)
			{
				Creature mostHated = npc.getAI().getAggroList().getMostHatedCreature();
				
				if (mostHated != null)
				{
					if (mostHated == attacker)
					{
						if (Rnd.get(100) < 33)
						{
							L2Skill DDMagic1 = getNpcSkillByType(npc, NpcSkillType.DD_MAGIC1);
							
							npc.getAI().addCastDesire(attacker, DDMagic1, 1000000);
						}
						if (Rnd.get(100) < 33)
						{
							L2Skill DDMagic2 = getNpcSkillByType(npc, NpcSkillType.DD_MAGIC2);
							
							npc.getAI().addCastDesire(attacker, DDMagic2, 1000000);
						}
						L2Skill debuff = getNpcSkillByType(npc, NpcSkillType.DEBUFF);
						
						if (Rnd.get(100) < 33 && getAbnormalLevel(attacker, debuff) <= 0)
						{
							npc.getAI().addCastDesire(attacker, debuff, 1000000);
						}
					}
				}
			}
		}
		
		super.onAttacked(npc, attacker, damage, skill);
	}
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if ((called.getAI().getLifeTime() > 7 && attacker instanceof Playable) && called.getAI().getCurrentIntention().getType() != IntentionType.ATTACK)
		{
			if (called.distance2D(attacker) > 100)
			{
				if (Rnd.get(100) < 33)
				{
					L2Skill DDMagic1 = getNpcSkillByType(called, NpcSkillType.DD_MAGIC1);
					
					called.getAI().addCastDesire(attacker, DDMagic1, 1000000);
				}
				if (Rnd.get(100) < 33)
				{
					L2Skill DDMagic2 = getNpcSkillByType(called, NpcSkillType.DD_MAGIC2);
					
					called.getAI().addCastDesire(attacker, DDMagic2, 1000000);
				}
			}
			
			L2Skill debuff = getNpcSkillByType(called, NpcSkillType.DEBUFF);
			if (Rnd.get(100) < 33 && getAbnormalLevel(attacker, debuff) <= 0)
			{
				called.getAI().addCastDesire(attacker, debuff, 1000000);
			}
		}
		
		super.onClanAttacked(caller, called, attacker, damage, skill);
	}
}
