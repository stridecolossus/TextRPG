package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.Vehicle.AbstractVehicle;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Trail;

/**
 * Movement mode for a {@link AbstractVehicle}.
 * @author Sarge
 */
public class VehicleMovementMode extends AbstractEqualsObject implements MovementMode {
	private final AbstractVehicle vehicle;

	/**
	 * Constructor.
	 * @param vehicle Vehicle
	 */
	public VehicleMovementMode(AbstractVehicle vehicle) {
		this.vehicle = notNull(vehicle);
	}

	@Override
	public Thing mover() {
		return vehicle;
	}

	@Override
	public List<Transaction> transactions(int cost) {
		return List.of();
	}

	@Override
	public void move(Exit exit) throws ActionException {
		// Check vehicle can traverse the exit
		if(exit.link().isEntityOnly()) throw ActionException.of("vehicle.entity.only");
		if(!vehicle.isValid(exit)) throw ActionException.of("vehicle.invalid.terrain");

		// TODO - check is 'driver' if capacity > 1
		// TODO - check steep slope, flooded? ditto mount mode

		// Move vehicle and contents
		vehicle.move(exit.destination());
	}

	@Override
	public Percentile noise() {
		return vehicle.noise();
	}

	@Override
	public Trail trail() {
		return vehicle.trail();
	}

	@Override
	public Percentile tracks() {
		return vehicle.tracks();
	}
}
