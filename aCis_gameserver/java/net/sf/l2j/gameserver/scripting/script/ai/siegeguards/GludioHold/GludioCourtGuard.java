package net.sf.l2j.gameserver.scripting.script.ai.siegeguards.GludioHold;

import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.L2Skill;

public class GludioCourtGuard extends GludioHold
{
	public GludioCourtGuard()
	{
		super("ai/siegeguards/GludioHold");
	}
	
	public GludioCourtGuard(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35069,
		35111,
		35153,
		35195,
		35238,
		35285,
		35329,
		35474,
		35521
	};
	
	@Override
	public void onSpelled(Npc npc, Player caster, L2Skill skill)
	{
		if (skill == FrequentSkill.SEAL_OF_RULER.getSkill())
			npc.getAI().addAttackDesire(caster, 50000);
	}
}
