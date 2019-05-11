package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to damage or destroy a breakable object.
 * @author Sarge
 */
@Component
public class DamageAction extends AbstractAction {
	/**
	 * Damages an object.
	 * @param actor		Actor
	 * @param obj		Object to damage
	 * @return Response
	 * @throws ActionException if the object is being carried or the actor does not have an equipped weapon
	 */
	@RequiresActor
	public Response damage(Entity actor, WorldObject obj) throws ActionException {
		// Check can be damaged
		if(!obj.descriptor().isFixture()) throw ActionException.of("damage.invalid.object");

		// Check weapon equipped
		// TODO - factor weapon check (and not broken) to @Weapon?
		final Weapon weapon = actor.contents().equipment().weapon().orElseThrow(() -> ActionException.of("damage.requires.weapon"));

		// Check available stamina
		// TODO - use combat helper, sta cost = weapon weight ~ modifiers (or another attribute?)

		// Apply damage
		final var damage = weapon.descriptor().damage();
		final int amount = (int) damage.amount().evaluate(actor.model());
		obj.damage(damage.type(), amount);
		weapon.use();

		// Build response
		if(obj.isAlive()) {
			return AbstractAction.response("damage.object.destroyed", obj.name());
		}
		else {
			return AbstractAction.response("damage.not.destroyed", obj.name());
		}
	}

	/**
	 * Damages a container and drops its contents to the current location if it is destroyed.
	 * @param actor			Actor
	 * @param container		Container to damage
	 * @return Response
	 * @throws ActionException if the container is being carried or the actor does not have an equipped weapon
	 */
	@RequiresActor
	public Response damage(Entity actor, Container container) throws ActionException {
		// Delegate
		final Response response = damage(actor, (WorldObject) container);

		// Drop contents to location
		if(!container.isAlive()) {
			// TODO
			//final var loc = actor.destination().location().contents();
			//container.contents().stream().forEach(loc::add);
		}

		return response;
	}
}
