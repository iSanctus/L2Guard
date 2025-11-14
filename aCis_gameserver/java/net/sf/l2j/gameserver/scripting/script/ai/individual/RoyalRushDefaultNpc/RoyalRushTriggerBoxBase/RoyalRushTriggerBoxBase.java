package net.sf.l2j.gameserver.scripting.script.ai.individual.RoyalRushDefaultNpc.RoyalRushTriggerBoxBase;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.script.ai.individual.RoyalRushDefaultNpc.RoyalRushDefaultNpc;

public class RoyalRushTriggerBoxBase extends RoyalRushDefaultNpc
{
	public RoyalRushTriggerBoxBase()
	{
		super("ai/individual/RoyalRushDefaultNpc/RoyalRushTriggerBoxBase");
	}
	
	public RoyalRushTriggerBoxBase(String descr)
	{
		super(descr);
	}
	
	@Override
	public void onCreated(Npc npc)
	{
		npc._i_ai0 = 0;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		npc.deleteMe();
		
		return null;
	}
}
