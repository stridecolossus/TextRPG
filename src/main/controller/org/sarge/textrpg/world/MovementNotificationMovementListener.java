package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.MovementNotification;
import org.springframework.stereotype.Component;

/**
 * Movement listener that generates movement notifications.
 * @author Sarge
 */
@Component
public class MovementNotificationMovementListener implements MovementController.Listener {
	private final EmissionController controller;

	/**
	 * Constructor.
	 * @param controller Emission controller
	 */
	public MovementNotificationMovementListener(EmissionController controller) {
		this.controller = notNull(controller);
	}

	@Override
	public void update(Entity actor, Exit exit, Location prev) {
		final Direction dir = exit.direction();
		broadcast(prev, actor, dir, false);
		broadcast(exit.destination(), actor, dir.reverse(), true);
	}

	/**
	 * Broadcasts a movement notification.
	 * @param loc			Location
	 * @param actor			Moving actor
	 * @param dir			Movement direction
	 * @param arrival		Whether arrival or departure
	 */
	private void broadcast(Location loc, Entity actor, Direction dir, boolean arrival) {
		final MovementNotification notification = new MovementNotification(actor, dir, arrival);
		controller.broadcast(actor, actor.location(), notification, notification.actor().visibility());
	}
}
