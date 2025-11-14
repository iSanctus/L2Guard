package net.sf.l2j.gameserver.model.actor.move;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.MoveType;
import net.sf.l2j.gameserver.enums.boats.BoatState;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.network.serverpackets.OnVehicleCheckLocation;
import net.sf.l2j.gameserver.network.serverpackets.VehicleDeparture;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;
import net.sf.l2j.gameserver.network.serverpackets.VehicleStarted;

public class BoatMove extends CreatureMove<Boat>
{
	private BoatLocation[] _currentPath;
	private int _pathIndex;
	
	public BoatMove(Boat actor)
	{
		super(actor);
		
		// Boats simply don't bother about other movements.
		addMoveType(MoveType.FLY);
	}
	
	@Override
	public void stop()
	{
		cancelMoveTask();
		
		_actor.broadcastPacket(new VehicleStarted(_actor, 0));
		_actor.broadcastPacket(new VehicleInfo(_actor));
	}
	
	@Override
	public boolean moveToNextRoutePoint()
	{
		return false;
	}
	
	@Override
	public boolean updatePosition(boolean firstRun)
	{
		final boolean result = super.updatePosition(firstRun);
		
		// Refresh passengers positions.
		_actor.getPassengers().forEach(player ->
		{
			player.setXYZ(_actor);
			player.revalidateZone(false);
			
			player.sendPacket(new OnVehicleCheckLocation(_actor));
		});
		
		return result;
	}
	
	public void onArrival()
	{
		if (_pathIndex < _currentPath.length)
		{
			_actor.getEngine().broadcast(_currentPath[_pathIndex].getArrivalMessages());
			
			// Increment the path index.
			_pathIndex++;
			
			if (_pathIndex == _currentPath.length - 1)
			{
				_actor.getEngine().setState(BoatState.READY_TO_MOVE_TO_DOCK);
				return;
			}
			
			if (_pathIndex == _currentPath.length)
			{
				_actor.getEngine().setState(BoatState.DOCKED);
				
				// Stop the Boat.
				stop();
				return;
			}
			
			// We are still on path, move to the next BoatLocation segment.
			moveToNextSegment();
		}
	}
	
	public void moveToBoatLocation(BoatLocation loc)
	{
		// Feed Boat move speed and rotation based on BoatLocation parameter.
		if (loc.getMoveSpeed() > 0)
			_actor.getStatus().setMoveSpeed(loc.getMoveSpeed());
		
		if (loc.getRotationSpeed() > 0)
			_actor.getStatus().setRotationSpeed(loc.getRotationSpeed());
		
		// Move the boat to the next destination.
		moveToLocation(loc, false);
		
		// Broadcast the movement (angle change, speed change, destination).
		_actor.broadcastPacket(new VehicleDeparture(_actor));
	}
		private void moveToNextSegment()
		{
			if (_pathIndex < 0 || _pathIndex >= _currentPath.length)
			{
				LOGGER.warn("BoatMove: Invalid _pathIndex " + _pathIndex + " for path length " + _currentPath.length);
	
				if (_actor != null)
				{
					_actor.getMove().stop();
	
					if (_actor.getAI() != null)
					{
						IntentionType type = _actor.getAI().getCurrentIntention().getType();
	
						if (type != IntentionType.IDLE && type != IntentionType.FOLLOW)
							LOGGER.info("Boat AI is still in action mode: " + type + ", but no override method available.");
					}
				}
	
				_pathIndex = 0;
				return;
			}
	
			final BoatLocation loc = _currentPath[_pathIndex];
	
			_actor.getEngine().broadcast(loc.getDepartureMessages());
	
			moveToBoatLocation(loc);
		}	
	/**
	 * Feed this {@link BoatMove} with a {@link BoatLocation} array, then trigger the {@link Boat} movement using the first BoatLocation of the array.
	 * @param path : The BoatLocation array used as path.
	 */
	public void executePath(BoatLocation... path)
	{
		// Initialize values.
		_pathIndex = 0;
		_currentPath = path.clone();
		
		// Move the Boat to first encountered BoatLocation.
		moveToNextSegment();
		
		// Broadcast the starting movement.
		_actor.broadcastPacket(new VehicleStarted(_actor, 1));
	}
}