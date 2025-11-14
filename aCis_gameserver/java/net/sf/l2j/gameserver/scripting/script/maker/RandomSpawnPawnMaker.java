package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class RandomSpawnPawnMaker extends DefaultMaker
{
	public RandomSpawnPawnMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onStart(NpcMaker maker)
	{
		if (!maker.isOnStart())
			return;
		
		for (MultiSpawn ms : maker.getSpawns())
		{
			if (ms.getTotal() != ms.getSpawned())
			{
				long toSpawnCount = ms.getTotal() - ms.getSpawned();
				for (long i = 0; i < toSpawnCount; i++)
					if (maker.getMaximumNpc() - maker.getNpcsAlive() > 0)
						ms.doSpawn(false);
			}
		}
	}
	
	@Override
	public void onNpcDeleted(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		final MultiSpawn rndMs = Rnd.get(maker.getSpawns());
		
		final long i2 = rndMs.getTotal() - rndMs.getSpawned();
		if (i2 > 0)
		{
			if (maker.getMaximumNpc() - maker.getNpcsAlive() > 0)
			{
				ThreadPool.schedule(() ->
				{
					if (rndMs.getDecayed() > 0)
						rndMs.doRespawn();
					else
						rndMs.doSpawn(false);
				}, rndMs.calculateRespawnDelay() * 1000);
			}
		}
	}
}
