package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorPhysicalSpecial;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;

public class WarriorFleeFromClanAggressive extends WarriorPhysicalSpecial
{
	public WarriorFleeFromClanAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorPhysicalSpecial");
	}
	
	public WarriorFleeFromClanAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21443,
		21446,
		21447,
		21449
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
	public void onClanDied(Npc caller, Npc called, Creature killer)
	{
		called.getAI().addFleeDesire(caller, getNpcIntAIParam(called, "Distance"), 10000);
		
		super.onClanDied(caller, called, killer);
	}
}