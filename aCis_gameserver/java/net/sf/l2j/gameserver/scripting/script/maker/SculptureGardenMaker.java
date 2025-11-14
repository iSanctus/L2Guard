package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class SculptureGardenMaker extends DefaultMaker
{
	public SculptureGardenMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onMakerScriptEvent(String name, NpcMaker maker, int int1, int int2)
	{
		if (name.equalsIgnoreCase("10005"))
		{
			final MultiSpawn def0 = maker.getSpawns().get(0);
			if (def0 != null)
				def0.sendScriptEvent(11038, 0, 0);
		}
	}
}