package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Map;

import org.sarge.lib.collection.Pair;
import org.sarge.lib.collection.StrictMap;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Controller for objects.
 * @author Sarge
 */
@Controller
public class ObjectController {
	private final Map<WorldObject, Pair<Location, Location>> portals = new StrictMap<>();

	private final Event.Queue queue;

	private Duration def = Duration.ofHours(1);

	/**
	 * Constructor.
	 * @param manager Queue manager for object events
	 */
	public ObjectController(Event.Queue.Manager manager) {
		this.queue = manager.queue("queue.objects");
	}

	/**
	 * Sets the default decay duration.
	 * @param def Default
	 * @throws IllegalArgumentException if the given duration is not one-or-more
	 */
	@Autowired
	public void setDefaultDecay(@Value("${decay.default}") Duration def) {
		if(def.isZero() || def.isNegative()) throw new IllegalArgumentException("Invalid default decay duration: " + def);
		this.def = notNull(def);
	}

	/**
	 * Registers a decay event for the given object.
	 * @param obj Object to decay
	 * @see WorldObject#decay()
	 */
	public void decay(WorldObject obj) {
		// Determine decay duration
		Duration duration = obj.descriptor().properties().decay();
		if(duration.isZero()) {
			duration = def;
		}

		// Register decay event
		final Event decay = () -> {
			obj.decay();
			return false;
		};
		register(decay, duration);
	}

	/**
	 * Registers a reset event for the given object.
	 * @param obj 		Object to reset
	 * @param reset		Reset call-back
	 * @return Event reference
	 */
	public Event.Reference reset(WorldObject obj, Event reset) {
		final Duration duration = obj.descriptor().properties().reset();
		return register(reset, duration);
	}

	/**
	 * Finds the other side of the given portal object.
	 * @param loc Location
	 * @param obj Portal object
	 * @return Other side
	 */
	public Location other(Location loc, WorldObject obj) {
		// Lookup entry
		final var pair = portals.get(obj);
		if(pair != null) {
			if(pair.left() == loc) {
				return pair.right();
			}
			else {
				return pair.left();
			}
		}

		// Otherwise find other side of the portal
		final Location other = loc.exits().stream()
			.filter(exit -> exit.link().controller().map(c -> c == obj).orElse(false))
			.map(Exit::destination)
			.findAny()
			.orElseThrow(() -> new IllegalStateException(String.format("Cannot find other side of portal: portal=%s loc=%s", obj, loc)));

		// Add new entries for both sides of the portal
		portals.put(obj, Pair.of(loc, other));

		return other;
	}

	/**
	 * Registers an event.
	 * @param event			Event call-back
	 * @param duration		Duration
	 * @return Event reference
	 */
	public Event.Reference register(Event event, Duration duration) {
		return queue.add(event, duration);
	}
}
