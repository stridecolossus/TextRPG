package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Event.Queue;
import org.springframework.stereotype.Controller;

/**
 * Ambient events controller.
 * @author Sarge
 */
@Controller
public class AmbientController implements MovementController.Listener {
	private final Map<Entity, Event.Holder> events = new StrictMap<>();
	private final Event.Queue queue;

	/**
	 * Constructor.
	 * @param queue Queue for ambient events
	 */
	public AmbientController(Queue queue) {
		this.queue = notNull(queue);
	}

	@Override
	public void update(Entity actor, Exit exit, Location prev) {
		// Ignore if same area
		final Location loc = actor.location();
		if(!loc.isTransition(prev)) {
			return;
		}

		// Register ambient event
		final Event.Holder holder = events.computeIfAbsent(actor, key -> new Event.Holder());
		loc.area().ambient().ifPresentOrElse(ambient -> start(actor, ambient, holder), () -> holder.cancel());

		// Remove stale entries
		clean();
	}

	@Override
	public boolean isPlayerOnly() {
		return true;
	}

	/**
	 * Registers a new ambient event.
	 * @param actor			Actor
	 * @param ambient		Ambient event
	 * @param holder		Event holder
	 */
	private void start(Entity actor, AmbientEvent ambient, Event.Holder holder) {
		final Event event = () -> {
			actor.alert(ambient.description());
			return ambient.isRepeating();
		};
		final Event.Reference ref = queue.add(event, ambient.period());
		holder.set(ref);
	}

	/**
	 * Removes stale entries.
	 */
	private void clean() {
		events.keySet().removeIf(StreamUtil.not(Entity::isAlive));
	}
}
