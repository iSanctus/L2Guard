package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class NoOnStartMaker extends DefaultMaker
{
	public NoOnStartMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onStart(NpcMaker maker)
	{
		// Do nothing.
	}
}