package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class FrintezzaEvilateMaker extends DefaultMaker
{
	public FrintezzaEvilateMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onMakerScriptEvent(String name, NpcMaker maker, int int1, int int2)
	{
		if (name.equalsIgnoreCase("1000"))
			maker.deleteAll();
		else if (name.equalsIgnoreCase("1001"))
		{
			if (checkHasSpawnCondition(maker))
				return;
			
			for (MultiSpawn ms : maker.getSpawns())
			{
				long toSpawnCount = ms.getTotal() - ms.getSpawned();
				
				for (Npc npc : ms.getNpcs())
				{
					if (npc.isDecayed())
					{
						npc.scheduleRespawn(int1 * 1000L);
						toSpawnCount--;
					}
				}
				
				for (long i = 0; i < toSpawnCount; i++)
				{
					ThreadPool.schedule(() ->
					{
						if (ms.getDecayed() > 0)
							ms.doRespawn();
						else
							ms.doSpawn(false);
					}, int1 * 1000L);
				}
			}
		}
	}
}