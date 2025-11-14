package net.sf.l2j.gameserver.scripting.script.ai.siegeguards.GludioHold;

import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.L2Skill;

public class GludioSirCronenberg extends GludioHold
{
	public GludioSirCronenberg()
	{
		super("ai/siegeguards/GludioHold");
	}
	
	public GludioSirCronenberg(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35065,
		35107,
		35149,
		35191,
		35281,
		35324,
		35516
	};
	
	@Override
	public void onSpelled(Npc npc, Player caster, L2Skill skill)
	{
		if (skill == FrequentSkill.SEAL_OF_RULER.getSkill() && getPledgeCastleState(npc, caster) != 2)
			npc.getAI().addAttackDesire(caster, 50000);
	}
}
