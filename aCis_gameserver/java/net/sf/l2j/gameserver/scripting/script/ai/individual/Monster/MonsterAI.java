package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;

public class MonsterAI extends DefaultNpc
{
	public MonsterAI()
	{
		super("ai");
	}
	
	public MonsterAI(String descr)
	{
		super(descr);
	}
	
	@Override
	public void onAttackFinished(Npc npc, Creature target)
	{
		if (target instanceof Summon && target.isDead())
		{
			final Player player = target.getActingPlayer();
			if (player != null)
				npc.getAI().addAttackDesire(player, 500);
		}
	}
}