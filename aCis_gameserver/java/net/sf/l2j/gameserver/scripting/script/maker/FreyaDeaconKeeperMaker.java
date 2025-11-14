package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;

public class FreyaDeaconKeeperMaker extends DefaultMaker
{
	public FreyaDeaconKeeperMaker(String name)
	{
		super(name);
	}
	
	@Override
	public void onMakerScriptEvent(String name, NpcMaker maker, int int1, int int2)
	{
		final MultiSpawn def0 = maker.getSpawns().get(0);
		
		if (name.equalsIgnoreCase("10025"))
		{
			if (def0 != null)
				def0.sendScriptEvent(10026, 0, 0);
		}
		else if (name.equalsIgnoreCase("10005"))
		{
			if (def0 != null)
				def0.sendScriptEvent(10005, 0, 0);
		}
		else if (name.equalsIgnoreCase("11037"))
		{
			if (def0 != null)
				def0.sendScriptEvent(11037, 0, 0);
		}
	}
}