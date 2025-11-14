package net.sf.l2j.gameserver.scripting.script.ai.siegablehall;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.network.NpcStringId;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Agit03Alfred extends DefaultNpc
{
	public Agit03Alfred()
	{
		super("ai/siegeablehall");
	}
	
	public Agit03Alfred(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35630
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		npc.broadcastNpcShout(NpcStringId.ID_1010636);
		
		npc._i_ai0 = 0;
		
		super.onCreated(npc);
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
			npc.getAI().addAttackDesire(attacker, (((damage * 1.0) / npc.getStatus().getMaxHp()) / 0.05) * 1000);
		
		if (npc.distance2D(attacker) > 300 && Rnd.get((25 * 30)) < 1)
			npc.getAI().addCastDesire(attacker, getNpcSkillByType(npc, NpcSkillType.DD_MAGIC), 1000000);
	}
	
	@Override
	public void onMyDying(Npc npc, Creature killer)
	{
		npc.broadcastNpcShout(NpcStringId.ID_1010625);
		
		// TODO Area
		// gg::Area_SetOnOff(AreaName,0);
	}
}
