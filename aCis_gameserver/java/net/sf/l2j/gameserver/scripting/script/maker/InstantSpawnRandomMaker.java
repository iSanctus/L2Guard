package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.manager.SpawnManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class InstantSpawnRandomMaker extends DefaultMaker
{
	public InstantSpawnRandomMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onNpcDeleted(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		if (maker.getNpcsAlive() == 0)
		{
			final int i1 = Rnd.get(maker.getMakerMemo().getInteger("maker_cnt"));
			if (i1 == 0)
			{
				final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker(maker.getMakerMemo().get("maker_name1"));
				if (maker0 != null)
					maker0.getMaker().onMakerScriptEvent("1001", maker0, (int) ms.calculateRespawnDelay(), 0);
			}
			else if (i1 == 1)
			{
				final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker(maker.getMakerMemo().get("maker_name2"));
				if (maker0 != null)
					maker0.getMaker().onMakerScriptEvent("1001", maker0, (int) ms.calculateRespawnDelay(), 0);
			}
			else if (i1 == 2)
			{
				final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker(maker.getMakerMemo().get("maker_name3"));
				if (maker0 != null)
					maker0.getMaker().onMakerScriptEvent("1001", maker0, (int) ms.calculateRespawnDelay(), 0);
			}
		}
	}
}