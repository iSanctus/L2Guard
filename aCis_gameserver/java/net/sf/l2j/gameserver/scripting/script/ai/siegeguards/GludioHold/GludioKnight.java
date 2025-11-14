package net.sf.l2j.gameserver.scripting.script.ai.siegeguards.GludioHold;

import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.L2Skill;

public class GludioKnight extends GludioHold
{
	public GludioKnight()
	{
		super("ai/siegeguards/GludioHold");
	}
	
	public GludioKnight(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35066,
		35108,
		35150,
		35192,
		35235,
		35282,
		35326,
		35471,
		35518
	};
	
	@Override
	public void onSpelled(Npc npc, Player caster, L2Skill skill)
	{
		if (skill == FrequentSkill.SEAL_OF_RULER.getSkill() && getPledgeCastleState(npc, caster) != 2)
			npc.getAI().addAttackDesire(caster, 50000);
	}
}
