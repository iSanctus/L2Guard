package net.sf.l2j.gameserver.scripting.script.ai.individual.RoyalRushDefaultNpc.RoyalRushTriggerBoxBase;

import java.util.Calendar;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.manager.SpawnManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;
import net.sf.l2j.gameserver.network.NpcStringId;

public class RoyalRushTriggerBox3 extends RoyalRushTriggerBoxBase
{
	public RoyalRushTriggerBox3()
	{
		super("ai/individual/RoyalRushDefaultNpc/RoyalRushTriggerBoxBase");
	}
	
	public RoyalRushTriggerBox3(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		31470,
		31475,
		31480,
		31485
	};
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final int i0 = Calendar.getInstance().get(Calendar.MINUTE);
		
		if (i0 > 49 && i0 < 60)
		{
			npc.broadcastNpcSay(NpcStringId.ID_1010552);
			return null;
		}
		if (npc._i_ai0 == 1)
			return null;
		
		npc._i_ai0 = 1;
		
		switch (Rnd.get(2))
		{
			case 0:
			{
				final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker(getNpcStringAIParamOrDefault(npc, "room_event_1", "1rd_type1_c"));
				if (maker0 != null)
				{
					maker0.getMaker().onMakerScriptEvent("1000", maker0, 0, 0);
					maker0.getMaker().onMakerScriptEvent("1001", maker0, 0, 0);
				}
				break;
			}
			case 1:
			{
				final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker(getNpcStringAIParamOrDefault(npc, "room_event_2", "1rd_type2_c"));
				if (maker0 != null)
				{
					maker0.getMaker().onMakerScriptEvent("1000", maker0, 0, 0);
					maker0.getMaker().onMakerScriptEvent("1001", maker0, 0, 0);
				}
				break;
			}
		}
		
		return super.onFirstTalk(npc, player);
	}
}
