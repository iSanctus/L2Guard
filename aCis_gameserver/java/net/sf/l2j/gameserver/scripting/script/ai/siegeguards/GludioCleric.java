package net.sf.l2j.gameserver.scripting.script.ai.siegeguards;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class GludioCleric extends DefaultNpc
{
	public GludioCleric()
	{
		super("ai/siegeguards");
	}
	
	public GludioCleric(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35018,
		35028,
		35038,
		35048,
		35058,
		35068,
		35110,
		35152,
		35194,
		35237,
		35284,
		35328,
		35473,
		35520
	};
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (caller.getStatus().getHpRatio() < 0.6 && Rnd.get(100) < 20)
			called.getAI().addCastDesire(caller, getNpcSkillByType(called, NpcSkillType.MAGIC_HEAL), 1000000);
	}
}