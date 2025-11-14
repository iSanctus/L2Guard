package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.subtype.CastleZoneType;

/**
 * A zone extending {@link CastleZoneType}, which fires a task on the first {@link Creature} entrance, notably used by castle slow traps.<br>
 * <br>
 * This task slows down {@link Player}s.
 */
public class SwampZone extends CastleZoneType
{
	private int _moveBonus = -50;
	
	public SwampZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("move_bonus"))
			_moveBonus = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		// Castle traps are active only during siege, or if they're activated.
		if (getCastle() != null && (!isEnabled() || !getCastle().getSiege().isInProgress()))
			return;
		
		creature.setInsideZone(ZoneId.SWAMP, true);
		if (creature instanceof Player player)
			player.broadcastUserInfo();
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.SWAMP, false);
		if (creature instanceof Player player)
			player.broadcastUserInfo();
	}
	
	public int getMoveBonus()
	{
		return _moveBonus;
	}
}