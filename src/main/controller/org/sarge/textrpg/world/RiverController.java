package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Event.Queue;
import org.sarge.textrpg.world.CurrentLink.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Controller for objects and entities in a river current.
 * @author Sarge
 * @see CurrentLink
 */
@Component
public class RiverController {
	private static final int MAX = Current.FAST.ordinal() + 1;

	private final Event.Queue queue;
	private final MovementController controller;

	private Duration fast = Duration.ofSeconds(1);

	/**
	 * Constructor.
	 * @param queue 			Queue for current movement events
	 * @param controller		Movement controller
	 */
	public RiverController(Queue queue, MovementController controller) {
		this.queue = notNull(queue);
		this.controller = notNull(controller);
	}

	/**
	 * Sets the iteration period for the fastest river current.
	 * @param fast Fast current iteration period
	 */
	@Autowired
	public void setMovementPeriod(@Value("${river.fast.period}") Duration fast) {
		this.fast = notNull(fast);
	}

	/**
	 * Registers an object or entity that has entered a river current.
	 * @param thing		Object or entity
	 * @param exit		Exit with a river current link
	 * @throws ClassCastException if the exit is not a {@link CurrentLink}
	 */
	public void add(Thing thing, Exit exit) {
		final CurrentLink link = (CurrentLink) exit.link();
		final Parent current = thing.parent();
		final Event event = () -> move(thing, exit, current);
		final Duration duration = fast.multipliedBy(speed(link));
		queue.add(event, duration);
	}

	/**
	 * Determines the speed of the given current.
	 * @param link Current link
	 * @return Speed
	 */
	private static int speed(CurrentLink link) {
		switch(link.current()) {
		case SLOW:
		case MEDIUM:
		case FAST:
			return MAX - link.current().ordinal();

		default:
			return 1;
		}
	}

	/**
	 * Moves an object or entity in this current.
	 * @param thing		Object to entity to move
	 * @param exit		River exit to traverse
	 * @param parent	Parent at time of registration
	 */
	private boolean move(Thing thing, Exit exit, Parent parent) {
		// Ignore if moved in the meantime
		final Parent prev = thing.parent();
		if(prev != parent) {
			return false;
		}

		// Move object/entity
		thing.parent(exit.destination());

		// Generate notifications
		if(thing instanceof Entity) {
			// Notify moved entity
			final Entity entity = (Entity) thing;
			controller.notify(entity, exit, (Location) prev);
			entity.alert(Description.DISPLAY_LOCATION);
		}
		else {
			// Broadcast object arrival event
			final Description description = new Description("river.object.moved", thing.name());
			exit.destination().broadcast(null, description);
		}

		// Register again if still in a river current
		CurrentLink.find(exit.destination()).ifPresent(e -> add(thing, e));

		return false;
	}
}
