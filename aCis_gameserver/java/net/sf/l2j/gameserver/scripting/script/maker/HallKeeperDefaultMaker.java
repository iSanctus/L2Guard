package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class HallKeeperDefaultMaker extends DefaultMaker
{
	public HallKeeperDefaultMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onStart(NpcMaker maker)
	{
		// Disables onStart
	}
}
