package net.sf.l2j.gameserver.scripting.script.ai.individual.RoyalRushDefaultNpc;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.skills.L2Skill;

public class RoyalRushTrasureBox extends RoyalRushAfflict
{
	public RoyalRushTrasureBox()
	{
		super("ai/individual/RoyalRushDefaultNpc");
	}
	
	public RoyalRushTrasureBox(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		18256
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		npc.scheduleDespawn(300000);
		
		super.onCreated(npc);
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		npc.getAI().addFleeDesire(attacker, 150, 200);
	}
}
