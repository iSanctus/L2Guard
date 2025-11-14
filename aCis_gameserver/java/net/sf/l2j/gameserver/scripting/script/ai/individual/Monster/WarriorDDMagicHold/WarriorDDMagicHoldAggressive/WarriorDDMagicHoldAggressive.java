package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorDDMagicHold.WarriorDDMagicHoldAggressive;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorDDMagicHold.WarriorDDMagicHold;

public class WarriorDDMagicHoldAggressive extends WarriorDDMagicHold
{
	public WarriorDDMagicHoldAggressive()
	{
		super("ai/individual/Monster/WarriorDDMagicHold/WarriorDDMagicHoldAggressive");
	}
	
	public WarriorDDMagicHoldAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds = {
		
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		final IntentionType currentIntentionType = npc.getAI().getCurrentIntention().getType();
		if (currentIntentionType != IntentionType.ATTACK && currentIntentionType != IntentionType.CAST)
			npc.getAI().addCastDesireHold(creature, getNpcSkillByType(npc, NpcSkillType.DD_MAGIC), 1000000);
		
		if (creature instanceof Playable)
			npc.getAI().addAttackDesireHold(creature, 50);
	}
}