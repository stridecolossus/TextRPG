package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Event;

/**
 * Model for a set of transient information known by an actor.
 * @param <T> Data-type
 * @author Sarge
 */
public class TransientModel extends AbstractEqualsObject {
	private final Set<Object> data = new StrictSet<>();
	private final Event.Queue queue;

	/**
	 * Constructor.
	 * @param queue Queue for forget events
	 */
	public TransientModel(Event.Queue queue) {
		this.queue = notNull(queue);
	}

	/**
	 * Tests whether the given item is known.
	 * @param item Item
	 * @return Whether this model contains the given item
	 */
	public boolean contains(Object item) {
		return data.contains(item);
	}

	/**
	 * @return Known items
	 */
	public Stream<?> stream() {
		return data.stream();
	}

	/**
	 * Adds a known transient item to this model.
	 * @param item 			Item to add
	 * @param forget		Forget period
	 * @throws IllegalArgumentException if the item is already known
	 */
	public void add(Object item, Duration forget) {
		// Mark item as known
		data.add(item);

		// Register forget event
		final Event remove = () -> data.remove(item);
		queue.add(remove, forget);
	}
}
