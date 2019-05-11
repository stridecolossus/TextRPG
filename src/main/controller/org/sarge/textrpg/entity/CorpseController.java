package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.object.ObjectController;
import org.sarge.textrpg.object.WorldObject;
import org.springframework.stereotype.Component;

/**
 * Controller for killed entities.
 * @author Sarge
 */
@Component
public class CorpseController {
	private final ObjectController controller;

	/**
	 * Constructor.
	 * @param controller Object controller for corpse decay events
	 */
	public CorpseController(ObjectController controller) {
		this.controller = notNull(controller);
	}

	/**
	 * Creates a corpse for a killed entity.
	 * @param entity Killed entity
	 * @return New corpse or <tt>null</tt> if none
	 */
	public Corpse create(Entity entity) {
		// Ignore if no corpse
		final Race.Kill kill = entity.descriptor().race().kill();
		if(!kill.corpse().isPresent()) {
			return null;
		}

		// Enumerate corpse contents
		final var contents = entity.contents().select(WorldObject.class).collect(toList());

		// Create corpse
		final Corpse corpse = new Corpse(kill.corpse().get(), kill.butcher(), contents);
		corpse.parent(entity.location());

		// Register decay event
		controller.decay(corpse);

		return corpse;
	}
}
