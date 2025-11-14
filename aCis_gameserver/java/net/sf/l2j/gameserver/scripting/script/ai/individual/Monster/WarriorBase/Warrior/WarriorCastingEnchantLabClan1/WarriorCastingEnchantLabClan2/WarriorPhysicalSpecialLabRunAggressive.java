package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastingEnchantLabClan1.WarriorCastingEnchantLabClan2;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorPhysicalSpecialLabRunAggressive extends WarriorCastingEnchantLabClan2
{
	public WarriorPhysicalSpecialLabRunAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastingEnchantLabClan1/WarriorCastingEnchantLabClan2");
	}
	
	public WarriorPhysicalSpecialLabRunAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22063
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
			return;
		
		tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (npc.getStatus().getHpRatio() < 0.4)
		{
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.PHYSICAL_SPECIAL), 1000000);
			if (!npc.isInMyTerritory())
				npc.getAI().addMoveToDesire(npc.getSpawnLocation(), 1000000);
		}
		
		super.onAttacked(npc, attacker, damage, skill);
	}
}
