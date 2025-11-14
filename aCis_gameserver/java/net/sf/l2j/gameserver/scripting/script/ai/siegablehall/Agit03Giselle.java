package net.sf.l2j.gameserver.scripting.script.ai.siegablehall;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.NpcStringId;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Agit03Giselle extends DefaultNpc
{
	public Agit03Giselle()
	{
		super("ai/siegeablehall");
	}
	
	public Agit03Giselle(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35631
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		npc.broadcastNpcShout(NpcStringId.ID_1010637);
		
		startQuestTimer("1003", npc, null, 1000);
		
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
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("1003"))
		{
			npc.getAI().addCastDesire(npc, getNpcSkillByType(npc, NpcSkillType.SELF_RANGE_BUFF_A), 1000000);
			
			startQuestTimer("1003", npc, player, 300000);
		}
		
		return null;
	}
	
	@Override
	public void onMyDying(Npc npc, Creature killer)
	{
		npc.broadcastNpcShout(NpcStringId.ID_1010625);
		
		// TODO Area
		// gg::Area_SetOnOff(AreaName1,0);
		// gg::Area_SetOnOff(AreaName2,0);
	}
}
