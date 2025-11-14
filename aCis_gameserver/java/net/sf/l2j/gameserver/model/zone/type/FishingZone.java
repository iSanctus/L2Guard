package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;

/**
 * A zone extending {@link ZoneType}, used for fish points.
 */
public class FishingZone extends ZoneType
{
	public FishingZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		// Do nothing.
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		// Do nothing.
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}