package net.sf.l2j.gameserver.scripting.script.ai.siegeguards.GludioWizard;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class GludioWizard extends DefaultNpc
{
	public GludioWizard()
	{
		super("ai/siegeguards/GludioWizard");
	}
	
	public GludioWizard(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35019,
		35029,
		35039,
		35049,
		35059,
		35067,
		35109,
		35151,
		35193,
		35236,
		35283,
		35327,
		35472,
		35519
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (getPledgeCastleState(npc, creature) != 2 && creature instanceof Playable)
			npc.getAI().addCastDesire(creature, getNpcSkillByType(npc, NpcSkillType.DD_MAGIC), 1000000);
		
		if (npc.isInsideZone(ZoneId.PEACE))
		{
			npc.teleportTo(npc.getSpawnLocation(), 0);
			npc.removeAllAttackDesire();
		}
	}
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (getPledgeCastleState(called, attacker) != 2 && attacker instanceof Playable && Rnd.get(100) < 10)
			called.getAI().addCastDesire(attacker, getNpcSkillByType(called, NpcSkillType.DD_MAGIC), 1000000);
	}
}
