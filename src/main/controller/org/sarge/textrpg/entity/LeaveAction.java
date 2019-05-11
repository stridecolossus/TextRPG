package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to leave the current ferry or vehicle.
 * @author Sarge
 */
@Component
public class LeaveAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public LeaveAction() {
		super(Flag.OUTSIDE);
	}

	/**
	 * Leaves the current ferry or vehicle.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if not travelling
	 */
	@RequiresActor
	public Response leave(PlayerCharacter actor) throws ActionException {
		// Check boarded
		final boolean boarded = (actor.parent() instanceof Ferry) || (actor.parent() instanceof Vehicle);
		if(!boarded) throw ActionException.of("leave.not.boarded");

		// Leave parent
		final var loc = actor.location();
		actor.parent(loc);
		actor.movement(null);

		// TODO - start swimming

		// Build response
		return Response.OK;
	}
}
