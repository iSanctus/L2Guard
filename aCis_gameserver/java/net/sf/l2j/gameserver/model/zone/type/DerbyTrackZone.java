package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;

/**
 * A zone extending {@link ZoneType} used by Derby Track system.<br>
 * <br>
 * The zone shares peace, no summon and monster track behaviors.
 */
public class DerbyTrackZone extends ZoneType
{
	public DerbyTrackZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		if (creature instanceof Playable playable)
		{
			playable.setInsideZone(ZoneId.MONSTER_TRACK, true);
			playable.setInsideZone(ZoneId.PEACE, true);
			playable.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (creature instanceof Playable playable)
		{
			playable.setInsideZone(ZoneId.MONSTER_TRACK, false);
			playable.setInsideZone(ZoneId.PEACE, false);
			playable.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		}
	}
}