package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorRunAwayPhysicalSpecial;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;

public class WarriorRunAwayPhysicalSpecialAggressive extends WarriorRunAwayPhysicalSpecial
{
	public WarriorRunAwayPhysicalSpecialAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorRunAwayPhysicalSpecialAggressive");
	}
	
	public WarriorRunAwayPhysicalSpecialAggressive(String descr)
	{
		super(descr);
	}
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (creature instanceof Playable)
			tryToAttack(npc, creature);
	}
}