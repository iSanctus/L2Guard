package net.sf.l2j.gameserver.scripting.script.ai.siegablehall;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class AzitWateringMimic extends DefaultNpc
{
	public AzitWateringMimic()
	{
		super("ai/siegeablehall");
	}
	
	public AzitWateringMimic(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35593
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			if (damage == 0)
				damage = 1;
			
			npc.getAI().addAttackDesire(attacker, ((1.0 * damage) / (npc.getStatus().getLevel() + 7)) * 100);
		}
	}
}
