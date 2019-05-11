package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.stereotype.Component;

/**
 * Action to use {@link Furniture}.
 * @author Sarge
 */
@Component
@RequiresActor
public class FurnitureAction extends AbstractAction {
	/**
	 * Sits on a piece of furniture.
	 * @param actor			Actor
	 * @param furniture		Furniture to sit on
	 * @return Response
	 * @throws ActionException if the furniture cannot be sat on or is already occupied
	 */
	public Response sit(Entity actor, Furniture furniture) throws ActionException {
		return use(actor, furniture, Stance.RESTING);
	}

	/**
	 * Sleeps on a piece of furniture.
	 * @param actor			Actor
	 * @param furniture		Furniture to go to sleep on
	 * @return Response
	 * @throws ActionException if the furniture cannot be slept on or is already occupied
	 */
	public Response sleep(Entity actor, Furniture furniture) throws ActionException {
		return use(actor, furniture, Stance.SLEEPING);
	}

	/**
	 * Uses a piece of furniture.
	 * @param actor			Actor
	 * @param furniture		Furniture to use
	 * @param stance		Stance
	 * @return Response
	 * @throws ActionException if the furniture does not support the given stance or is already occupied
	 */
	private static Response use(Entity actor, Furniture furniture, Stance stance) throws ActionException {
		// Check stance
		if(!furniture.descriptor().isValid(stance)) throw ActionException.of(TextHelper.join("furniture.cannot", stance.name()));

		// Check whether occupied
		final var reason = furniture.contents().reason(actor);
		if(reason.isPresent()) throw ActionException.of(reason.get());

		// Add to furniture
		actor.parent(furniture);
		actor.model().stance(stance);

		// Build response
		return AbstractAction.response(TextHelper.join("action.furniture", stance.name()), furniture.name());
	}
}
