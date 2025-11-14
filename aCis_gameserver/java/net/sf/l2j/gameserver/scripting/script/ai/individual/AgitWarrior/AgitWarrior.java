package net.sf.l2j.gameserver.scripting.script.ai.individual.AgitWarrior;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class AgitWarrior extends DefaultNpc
{
	private static final L2Skill NPC_STRIKE = SkillTable.getInstance().getInfo(4032, 6);
	
	public AgitWarrior()
	{
		super("ai/individual/AgitWarrior");
	}
	
	public AgitWarrior(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35428,
		35618
	};
	
	@Override
	public void onMoveToFinished(Npc npc, int x, int y, int z)
	{
		npc.lookNeighbor();
	}
	
	@Override
	public void onNoDesire(Npc npc)
	{
		npc.getAI().addWanderDesire(5, 5);
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		if (player != null && (player.getClanId() != npc.getClanId() || player.getClanId() == 0))
		{
			npc.getAI().addAttackDesire(attacker, ((((double) damage) / npc.getStatus().getMaxHp()) / 0.05) * (attacker instanceof Player ? 100 : 10));
			
			if (Rnd.get(100) < 10)
				npc.getAI().addCastDesire(attacker, NPC_STRIKE, 1000000);
		}
	}
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		final Player player = creature.getActingPlayer();
		if (player != null && (player.getClanId() != npc.getClanId() || player.getClanId() == 0))
		{
			npc.getAI().addAttackDesire(player, 200);
		}
		else if (creature instanceof Attackable)
		{
			npc.getAI().addAttackDesire(creature, 200);
		}
	}
}