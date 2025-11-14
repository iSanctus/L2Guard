package net.sf.l2j.gameserver.scripting.script.ai.siegablehall;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.script.ai.individual.DefaultNpc;

public class AzitWateringGroud extends DefaultNpc
{
	public AzitWateringGroud()
	{
		super("ai/siegeablehall");
		
		addFirstTalkId(_npcIds);
	}
	
	public AzitWateringGroud(String descr)
	{
		super(descr);
		
		addFirstTalkId(_npcIds);
	}
	
	protected final int[] _npcIds =
	{
		35588,
		35589,
		35590,
		35591
	};
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return null;
	}
	
	@Override
	public void onMyDying(Npc npc, Creature killer)
	{
		broadcastScriptEventEx(npc, 5, 40000, killer.getObjectId(), 500);
	}
}
