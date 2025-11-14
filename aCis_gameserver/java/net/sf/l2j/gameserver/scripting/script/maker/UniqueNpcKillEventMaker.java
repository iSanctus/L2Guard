package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.data.manager.SpawnManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class UniqueNpcKillEventMaker extends DefaultMaker
{
	public UniqueNpcKillEventMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onNpcDeleted(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		final String uniqueNpcAlias = maker.getMakerMemo().get("unique_npc");
		
		if (npc.getTemplate().getAlias().equalsIgnoreCase(uniqueNpcAlias))
		{
			final int event = maker.getMakerMemo().getInteger("event");
			if (event == 0)
				maker.getMaker().onMakerScriptEvent("1000", maker, 0, 0);
			else if (event == 1)
			{
				final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker(maker.getMakerMemo().get("maker_name"));
				if (maker0 != null)
					maker0.getMaker().onMakerScriptEvent("1001", maker0, 0, 0);
			}
		}
		super.onNpcDeleted(npc, ms, maker);
	}
}