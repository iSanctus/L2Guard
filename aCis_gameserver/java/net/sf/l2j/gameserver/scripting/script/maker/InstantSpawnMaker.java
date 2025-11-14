package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class InstantSpawnMaker extends DefaultMaker
{
	public InstantSpawnMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onNpcDeleted(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		if (maker.getNpcsAlive() == 0)
			maker.getMaker().onMakerScriptEvent("1001", maker, 0, 0);
	}
}