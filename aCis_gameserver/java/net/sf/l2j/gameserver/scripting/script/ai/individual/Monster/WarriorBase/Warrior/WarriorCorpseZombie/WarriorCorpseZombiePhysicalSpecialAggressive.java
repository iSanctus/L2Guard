
package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCorpseZombie;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCorpseZombiePhysicalSpecialAggressive extends WarriorCorpseZombieDDMagic
{
	public WarriorCorpseZombiePhysicalSpecialAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCorpseZombie");
	}
	
	public WarriorCorpseZombiePhysicalSpecialAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21550,
		21408,
		21552,
		21561,
		22176
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
		{
			super.onSeeCreature(npc, creature);
			return;
		}
		
		if (getNpcIntAIParam(npc, "IsTeleport") != 0 && npc.getAI().getLifeTime() > 7 && npc.distance2D(creature) > 100 && npc.getAI().getCurrentIntention().getType() != IntentionType.ATTACK && Rnd.get(100) < 10)
		{
			npc.abortAll(false);
			npc.instantTeleportTo(creature.getPosition().clone(), 0);
			
			final L2Skill avTeleport = SkillTable.getInstance().getInfo(4671, 1);
			npc.getAI().addCastDesire(creature, avTeleport, 1000000);
		}
		
		tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
}