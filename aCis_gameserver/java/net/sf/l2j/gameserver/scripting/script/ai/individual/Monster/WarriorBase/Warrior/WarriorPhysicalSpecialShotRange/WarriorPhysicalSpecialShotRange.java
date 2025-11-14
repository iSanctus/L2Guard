package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorPhysicalSpecialShotRange;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.Warrior;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorPhysicalSpecialShotRange extends Warrior
{
	public WarriorPhysicalSpecialShotRange()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorPhysicalSpecialShotRange");
	}
	
	public WarriorPhysicalSpecialShotRange(String descr)
	{
		super(descr);
	}
	
	@Override
	public void onCreated(Npc npc)
	{
		npc._i_ai1 = 0;
		npc._i_ai3 = 0;
		
		super.onCreated(npc);
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		npc._i_ai3 = attacker.getObjectId();
		if (npc.distance2D(attacker) > 200)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.LONG_RANGE_PHYSICAL_SPECIAL), 1000000);
		if (Rnd.get(100) < 33)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.PHYSICAL_SPECIAL), 1000000);
		if (Rnd.get(100) < 33)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.SELF_RANGE_PHYSICAL_SPECIAL), 1000000);
		
		super.onAttacked(npc, attacker, damage, skill);
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("1002"))
		{
			Creature c0 = (Creature) World.getInstance().getObject(npc._i_ai3);
			if (c0 != null && npc.distance2D(c0) > 200)
				npc.getAI().addCastDesire(c0, getNpcSkillByType(npc, NpcSkillType.LONG_RANGE_PHYSICAL_SPECIAL), 1000000);
			else
				npc._i_ai3 = 0;
			
			startQuestTimer("1002", npc, null, 5000);
		}
		
		return null;
	}
}
