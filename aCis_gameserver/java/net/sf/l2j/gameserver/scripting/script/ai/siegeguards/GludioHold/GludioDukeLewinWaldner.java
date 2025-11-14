package net.sf.l2j.gameserver.scripting.script.ai.siegeguards.GludioHold;

import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.L2Skill;

public class GludioDukeLewinWaldner extends GludioHold
{
	public GludioDukeLewinWaldner()
	{
		super("ai/siegeguards/GludioHold");
	}
	
	public GludioDukeLewinWaldner(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35064,
		35106,
		35148,
		35190,
		35234,
		35280,
		35325,
		35470,
		35517
	};
	
	@Override
	public void onSpelled(Npc npc, Player caster, L2Skill skill)
	{
		if (skill == FrequentSkill.SEAL_OF_RULER.getSkill() && getPledgeCastleState(npc, caster) != 2)
			npc.getAI().addAttackDesire(caster, 50000);
	}
}
