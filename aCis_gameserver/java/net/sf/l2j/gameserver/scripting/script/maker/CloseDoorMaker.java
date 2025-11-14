package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class CloseDoorMaker extends DefaultMaker
{
	public CloseDoorMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onStart(NpcMaker maker)
	{
		maker.getMakerMemo().set("enabled", false);
		
		final String doorName = maker.getMakerMemo().get("DoorName");
		if (doorName != null)
		{
			final Door door = DoorData.getInstance().getDoor(doorName);
			if (door != null)
				door.addMakerEvent(maker);
		}
	}
	
	@Override
	public void onDoorEvent(Door door, NpcMaker maker)
	{
		if (!door.isOpened())
		{
			if (maker.getMakerMemo().getBool("enabled"))
				return;
			
			maker.getMakerMemo().set("enabled", true);
			
			for (MultiSpawn ms : maker.getSpawns())
			{
				long toSpawnCount = ms.getTotal() - ms.getSpawned();
				for (long i = 0; i < toSpawnCount; i++)
					if (maker.getMaximumNpc() - maker.getNpcsAlive() > 0)
						ms.doSpawn(false);
			}
		}
		else
		{
			if (!maker.getMakerMemo().getBool("enabled"))
				return;
			
			maker.getMakerMemo().set("enabled", false);
			
			maker.deleteAll();
		}
	}
	
	@Override
	public void onNpcCreated(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		if (!maker.getMakerMemo().getBool("enabled"))
			npc.deleteMe();
	}
	
	@Override
	public void onNpcDeleted(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		// Do nothing.
	}
}