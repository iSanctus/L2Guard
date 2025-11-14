package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.data.manager.SpawnManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class TyrannosaurusMaker extends VelociraptorMaker
{
	public TyrannosaurusMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onStart(NpcMaker maker)
	{
		maker.getMakerMemo().set("i_ai0", 0);
	}
	
	@Override
	public void onNpcDeleted(Npc npc, MultiSpawn ms, NpcMaker maker)
	{
		if (maker.getNpcsAlive() == 0)
			ThreadPool.schedule(() -> onTimer("1002", maker), 180000);
	}
	
	@Override
	public void onTimer(String name, NpcMaker maker)
	{
		if (name.equalsIgnoreCase("1002"))
		{
			final NpcMaker maker0 = SpawnManager.getInstance().getNpcMaker("rune20_mb2017_04m1");
			if (maker0 != null)
				maker0.getMaker().onMakerScriptEvent("11042", maker0, 0, 0);
		}
		super.onTimer(name, maker);
	}
}