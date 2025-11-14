package net.sf.l2j.gameserver.scripting.script.ai.individual.RoyalRushDefaultNpc;

import java.util.Calendar;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;

public class RoyalRushDefaultNpc extends DefaultNpc
{
	public RoyalRushDefaultNpc()
	{
		super("ai/individual/RoyalRushDefaultNpc");
	}
	
	public RoyalRushDefaultNpc(String descr)
	{
		super(descr);
	}
	
	@Override
	public void onCreated(Npc npc)
	{
		startQuestTimerAtFixedRate("3000", npc, null, 1000, 1000);
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("3000"))
		{
			final int i0 = Calendar.getInstance().get(Calendar.MINUTE);
			if (i0 == 54)
				npc.deleteMe();
		}
		
		return null;
	}
}
