package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.data.manager.SpawnManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;
import net.sf.l2j.gameserver.model.spawn.SpawnData;

public class SailrenMaker extends DefaultUseDBMaker
{
	public SailrenMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onMakerScriptEvent(String name, NpcMaker maker, int int1, int int2)
	{
		if (name.equalsIgnoreCase("11042"))
		{
			final MultiSpawn def0 = maker.getSpawns().get(0);
			if (def0 != null)
				def0.sendScriptEvent(11042, 1, 0);
		}
		super.onMakerScriptEvent(name, maker, int1, int2);
	}
	
	@Override
	public void onNpcDBInfo(MultiSpawn ms, SpawnData spawnData, NpcMaker maker)
	{
		if (ms.getSpawnData() != null && !ms.getSpawnData().checkDead())
		{
			if (maker.getMaximumNpc() > maker.getNpcsAlive())
			{
				if (ms.getSpawnData().getX() != -113091)
				{
					NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker("rune16_npc2017_01m1");
					if (maker0 != null)
						maker0.getMaker().onMakerScriptEvent("11047", maker0, 0, 0);
					
					maker0 = SpawnManager.getInstance().getNpcMaker("rune16_npc2017_13m1");
					if (maker0 != null)
						maker0.getMaker().onMakerScriptEvent("11047", maker0, 0, 0);
				}
				ms.doSpawn(false);
			}
		}
		else
			ms.doSpawn(false);
	}
	
	@Override
	public void onNpcDeleted(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker("rune16_npc2017_01m1");
		if (maker0 != null)
			maker0.getMaker().onMakerScriptEvent("11045", maker0, 0, 0);
		
		if (maker.getMaximumNpc() > maker.getNpcsAlive())
			npc.scheduleRespawn(ms.calculateRespawnDelay() * 1000);
	}
}