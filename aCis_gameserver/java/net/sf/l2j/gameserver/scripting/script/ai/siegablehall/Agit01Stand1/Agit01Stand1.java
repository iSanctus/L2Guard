package net.sf.l2j.gameserver.scripting.script.ai.siegablehall.Agit01Stand1;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Agit01Stand1 extends DefaultNpc
{
	public Agit01Stand1()
	{
		super("ai/siegeablehall/Agit01Stand1");
	}
	
	public Agit01Stand1(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35370,
		35371,
		35374,
	};
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
			npc.getAI().addAttackDesireHold(attacker, ((damage * 1.0 / npc.getStatus().getMaxHp()) / 0.05) * 100);
	}
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
			called.getAI().addAttackDesireHold(attacker, ((damage * 1.0 / called.getStatus().getMaxHp()) / 0.05) * 100);
	}
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (!(creature instanceof Playable))
			return;
		
		npc.getAI().addAttackDesireHold(creature, 200);
		
	}
	
	@Override
	public void onSeeSpell(Npc npc, Player caster, L2Skill skill, Creature[] targets, boolean isPet)
	{
		if (skill.getAggroPoints() > 0)
			npc.getAI().addAttackDesireHold(caster, (((skill.getAggroPoints() * 1.0 / npc.getStatus().getMaxHp()) / 0.05) * 150));
	}
}
