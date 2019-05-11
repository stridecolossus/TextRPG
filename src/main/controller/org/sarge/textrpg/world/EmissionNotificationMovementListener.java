package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;

import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.entity.Entity;
import org.springframework.stereotype.Component;

/**
 * Movement listener that broadcasts emission notifications to neighbouring locations.
 * @author Sarge
 */
@Component
public class EmissionNotificationMovementListener implements MovementController.Listener {
	private static final Emission[] EMISSIONS = Emission.values().clone();

	private final EmissionController controller;

	/**
	 * Constructor.
	 * @param controller Emissions controller
	 */
	public EmissionNotificationMovementListener(EmissionController controller) {
		this.controller = notNull(controller);
	}

	@Override
	public void update(Entity actor, Exit exit, Location prev) {
		// Generate notifications
		final var notifications = Arrays.stream(EMISSIONS)
			.map(e -> new EmissionNotification(e, actor.emission(e)))
			.filter(e -> !e.intensity().isZero())
			.collect(toSet());

		// Broadcast to neighbours
		if(!notifications.isEmpty()) {
			controller.broadcast(actor, notifications);
		}
	}
}
