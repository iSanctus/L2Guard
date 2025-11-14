package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;

/**
 * A zone extending {@link ZoneType} where restart isn't allowed.
 */
public class NoRestartZone extends ZoneType
{
	public NoRestartZone(final int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(final Creature creature)
	{
		if (creature instanceof Player player)
			player.setInsideZone(ZoneId.NO_RESTART, true);
	}
	
	@Override
	protected void onExit(final Creature creature)
	{
		if (creature instanceof Player player)
			player.setInsideZone(ZoneId.NO_RESTART, false);
	}
}