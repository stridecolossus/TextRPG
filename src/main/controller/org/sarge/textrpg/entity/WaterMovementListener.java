package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.world.CurrentLink;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.MovementController;
import org.sarge.textrpg.world.RiverController;
import org.springframework.stereotype.Component;

/**
 * Movement listener that handles moving in and out of water locations.
 * <ul>
 * <li>toggles swimming as necessary</li>
 * <li>registers entities that moved into a river current</li>
 * </ul>
 * @author Sarge
 */
@Component
public class WaterMovementListener implements MovementController.Listener {
	private final SwimmingController controller;
	private final RiverController river;

	/**
	 * Constructor.
	 * @param controller
	 */
	public WaterMovementListener(SwimmingController controller, RiverController river) {
		this.controller = notNull(controller);
		this.river = notNull(river);
	}

	@Override
	public boolean isPlayerOnly() {
		return true;
	}

	@Override
	public void update(Entity actor, Exit exit, Location prev) {
		if(actor.location().isWater()) {
			// Start swimming if moved into water
			final boolean frozen = actor.location().isFrozen();
			if(!frozen && (actor.model().stance() != Stance.SWIMMING)) {
				controller.start(actor);
				// TODO - register if boat
				CurrentLink.find(actor.location()).ifPresent(e -> river.add(actor, e));
			}
		}
		else {
			// Stop swimming if moved to land
			if(actor.model().stance() == Stance.SWIMMING) {
				controller.stop(actor);
			}
		}
	}
}
