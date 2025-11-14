package net.sf.l2j.gameserver.enums.boats;

import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public enum BoatSound
{
	ARRIVAL_DEPARTURE("itemsound.ship_arrival_departure"),
	LEAVE_5_MIN("itemsound.ship_5min"),
	LEAVE_1_MIN("itemsound.ship_1min");
	
	private String _sound;
	
	BoatSound(String sound)
	{
		_sound = sound;
	}
	
	public PlaySound get(Boat boat)
	{
		return new PlaySound(0, _sound, boat);
	}
}