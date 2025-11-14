package net.sf.l2j.gameserver.scripting.script.ai.siegablehall;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.network.NpcStringId;
import net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorAggressive.WarriorAggressive;

public class AzitWateringCrazyYeti extends WarriorAggressive
{
	public AzitWateringCrazyYeti()
	{
		super("ai/siegeablehall");
	}
	
	public AzitWateringCrazyYeti(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		35592
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		npc.broadcastNpcShout(NpcStringId.ID_1010627);
		
		super.onCreated(npc);
	}
}
