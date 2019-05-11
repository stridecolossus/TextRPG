package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.EmissionController;
import org.springframework.stereotype.Component;

/**
 * Blow a {@link Horn}.
 * @author Sarge
 */
@Component
public class BlowHornAction extends AbstractAction {
	/**
	 * Horn category.
	 */
	public static final String HORN = "horn";

	private final EmissionController controller;

	/**
	 * Constructor.
	 * @param controller Emissions controller
	 */
	public BlowHornAction(EmissionController controller) {
		this.controller = notNull(controller);
	}

	/**
	 * Blows a horn.
	 * @param actor		Actor
	 * @param obj		Object to blow
	 * @return Response
	 * @throws ActionException if the object is not a horn or does not match the actors alignment
	 */
	@RequiresActor
	public Response blow(Entity actor, WorldObject obj) throws ActionException {
		// Check object is a horn
		if(!obj.isCategory(HORN)) {
			throw ActionException.of("blow.invalid.object");
		}

		// Check alignment
		final Alignment alignment = obj.descriptor().properties().alignment();
		if((alignment != Alignment.NEUTRAL) && (alignment != actor.descriptor().alignment())) {
			throw ActionException.of("blow.invalid.alignment");
		}

		// Broadcast to this location and its neighbours
		final EmissionNotification notification = new EmissionNotification(HORN, Percentile.ONE);
		controller.broadcast(actor, Set.of(notification));

		// Build response
		return response("action.blow", obj.name());
	}
}
