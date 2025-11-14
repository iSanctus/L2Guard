package net.sf.l2j.gameserver.scripting.script.ai.siegablehall;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class AzitWateringMimicMagical extends DefaultNpc
{
	public AzitWateringMimicMagical()
	{
		super("ai/siegeablehall");
	}
	
	public AzitWateringMimicMagical(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35594
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (Rnd.get(9) < 1)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DD_MAGIC), 1000000);
	}
	
	@Override
	public void onUseSkillFinished(Npc npc, Creature creature, L2Skill skill, boolean success)
	{
		if (getNpcSkillByType(npc, NpcSkillType.DD_MAGIC) == skill && success)
			npc.deleteMe();
	}
}
