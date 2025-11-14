package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastHoldMagic;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;

public class WarriorCastHoldMagicAggressive extends WarriorCastHoldMagic
{
	public WarriorCastHoldMagicAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastHoldMagic");
	}
	
	public WarriorCastHoldMagicAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		20134,
		20105,
		20144,
		20057,
		20171,
		20351,
		20419,
		18008
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