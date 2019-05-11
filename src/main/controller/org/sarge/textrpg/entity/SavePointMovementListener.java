package org.sarge.textrpg.entity;

import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.MovementController;
import org.sarge.textrpg.world.Property;
import org.springframework.stereotype.Component;

/**
 * Save-point movement listener.
 * @author Sarge
 */
@Component
public class SavePointMovementListener implements MovementController.Listener {
	@Override
	public boolean isPlayerOnly() {
		return true;
	}

	@Override
	public void update(Entity actor, Exit exit, Location prev) {
		if(actor.isPlayer() && actor.location().isProperty(Property.SAVE_POINT)) {
			// TODO - save player
		}
	}
}
