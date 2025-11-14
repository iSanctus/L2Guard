package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.manager.SpawnManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;
import net.sf.l2j.gameserver.model.spawn.SpawnData;

public class DefaultUseDBMaker extends DefaultMaker
{
	public DefaultUseDBMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onStart(NpcMaker maker)
	{
		for (MultiSpawn ms : maker.getSpawns())
		{
			if (ms.getTotal() != ms.getSpawned())
				ms.loadDBNpcInfo();
		}
	}
	
	@Override
	public void onNpcDBInfo(MultiSpawn ms, SpawnData spawnData, NpcMaker maker)
	{
		if (ms.getTotal() - ms.getSpawned() > 0)
			ms.doSpawn(false);
	}
	
	@Override
	public void onNpcDeleted(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		ms.setDBLoaded(false);
		
		if (maker.getNpcsAlive() == 0)
		{
			final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker(maker.getMakerMemo().get("maker_name"));
			if (maker0 != null)
				maker0.getMaker().onMakerScriptEvent("1001", maker0, 0, 0);
		}
		
		if (maker.getMaximumNpc() - maker.getNpcsAlive() > 0)
		{
			ThreadPool.schedule(() ->
			{
				if (ms.getDecayed() > 0)
					ms.doRespawn();
				else
					ms.doSpawn(false);
			}, ms.calculateRespawnDelay() * 1000);
		}
	}
}