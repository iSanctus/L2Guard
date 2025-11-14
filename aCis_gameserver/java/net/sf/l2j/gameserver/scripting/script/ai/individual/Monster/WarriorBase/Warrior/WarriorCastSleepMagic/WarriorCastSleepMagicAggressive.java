package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastSleepMagic;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;

public class WarriorCastSleepMagicAggressive extends WarriorCastSleepMagic
{
	public WarriorCastSleepMagicAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastSleepMagic");
	}
	
	public WarriorCastSleepMagicAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		20673,
		20048,
		20353
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
			return;
		
		tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
}