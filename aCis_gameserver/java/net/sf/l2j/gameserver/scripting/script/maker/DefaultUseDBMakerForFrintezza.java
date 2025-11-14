package net.sf.l2j.gameserver.scripting.script.maker;

import net.sf.l2j.gameserver.model.spawn.MultiSpawn;
import net.sf.l2j.gameserver.model.spawn.NpcMaker;
import net.sf.l2j.gameserver.model.spawn.SpawnData;

public class DefaultUseDBMakerForFrintezza extends DefaultUseDBMaker
{
	public DefaultUseDBMakerForFrintezza(String name)
	{
		super(name);
	}
	
	@Override
	public void onNpcDBInfo(MultiSpawn ms, SpawnData spawnData, NpcMaker maker)
	{
		ms.doSpawn(true);
	}
}
