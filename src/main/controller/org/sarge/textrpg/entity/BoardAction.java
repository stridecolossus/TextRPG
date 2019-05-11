package org.sarge.textrpg.entity;

import org.sarge.lib.util.Util;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Ferry.Ticket;
import org.sarge.textrpg.entity.Vehicle.AbstractVehicle;
import org.sarge.textrpg.object.ObjectHelper;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Location;
import org.springframework.stereotype.Component;

/**
 * Action to board a {@link Ferry}, {@link Vehicle} or {@link Boat}.
 * @author Sarge
 */
@Component
public class BoardAction extends AbstractAction {
	/**
	 * Boards a ferry.
	 * @param actor		Actor
	 * @param ferry		Ferry to board
	 * @return Response
	 * @throws ActionException if the given actor is already a passenger, is in a vehicle, or does not possess the required ticket
	 */
	@RequiresActor
	public Response board(Entity actor, Ferry ferry) throws ActionException {
		// Check can board
		if(!(actor.parent() instanceof Location)) throw ActionException.of("board.cannot.board");
		if(actor.parent() == ferry) throw ActionException.of("board.already.passenger");

		// Init response
		final Description.Builder response = new Description.Builder("board.ferry").name(ferry.name());

		// Check for required ticket
		if(ferry.isTicketRequired()) {
			final Ticket ticket = actor.contents().select(Ticket.class).filter(t -> t.ferry() == ferry).findAny().orElseThrow(() -> ActionException.of("ferry.requires.ticket"));
			if(ticket.destination() == actor.location()) throw ActionException.of("ferry.already.destination");
			ObjectHelper.destroy(ticket);
		}

		// Board ferry
		// TODO - notify passengers?
		actor.parent(ferry);

		// Build response
		return Response.of(response.build());
	}

	/**
	 * Boards a vehicle.
	 * @param actor			Actor
	 * @param vehicle		Vehicle to board
	 * @return Response
	 * @throws ActionException if the vehicle cannot be boarded
	 */
	@RequiresActor
	public Response board(PlayerCharacter actor, AbstractVehicle vehicle) throws ActionException {
		// Check can be boarded
		vehicle.contents().reason(actor).map(ActionException::of).ifPresent(Util::rethrow);

		// Enter vehicle
		actor.parent(vehicle);
		actor.movement(new VehicleMovementMode(vehicle));

		// Build response
		final String key = vehicle.isRaft() ? "raft" : "vehicle";
		return response(TextHelper.join("board", key), vehicle.name());
	}
}
