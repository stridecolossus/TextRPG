package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.sarge.lib.collection.LoopIterator;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Event.Queue;
import org.sarge.textrpg.world.Location;
import org.springframework.stereotype.Controller;

/**
 * Controller for an automated {@link Ferry}.
 * @author Sarge
 */
@Controller
public class FerryController {
	private final Event.Queue queue;
	private final Map<Entity, Ferry.Ticket> tickets = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 * @param queue Queue for ferry movement events
	 */
	public FerryController(Queue queue) {
		this.queue = notNull(queue);
	}

	/**
	 * Starts an automated ferry.
	 * @param ferry			Ferry to automate
	 * @param strategy		Iteration policy
	 * @param period		Iteration period
	 */
	// TODO - duration ~ location, e.g. function/table?
	public void start(Ferry ferry, LoopIterator.Strategy strategy, Duration period) {
		final LoopIterator<Location> iterator = ferry.iterator(strategy);
		final Event move = () -> {
			ferry.move(iterator.next());
			disembark(ferry);
			return true;
		};
		queue.add(move, period);
	}

	/**
	 * Registers an entity travelling on the given ferry to a specified ticketed destination.
	 * @param actor			Actor
	 * @param ticket		Ticket
	 */
	public void register(Entity actor, Ferry.Ticket ticket) {
		assert ticket.ferry().isTicketRequired();
		assert !tickets.containsKey(actor);
		tickets.put(actor, ticket);
	}

	/**
	 * Disembark passengers that have reached their destination on the given ferry.
	 * @param ferry Ferry
	 */
	private void disembark(Ferry ferry) {
		// Enumerate entities that have reached their destination
		final Location dest = (Location) ferry.parent();
		final Set<Entity> entities;
		synchronized(tickets) {
			// Clear actors that are no longer travelling
			tickets.keySet().removeIf(actor -> actor.parent() != ferry);

			// Enumerate pending entities and remove from tracker
			entities = tickets.entrySet().stream().filter(entry -> entry.getValue().destination() == dest).map(Map.Entry::getKey).collect(Collectors.toSet());
			tickets.keySet().removeAll(entities);
		}

		// Disembark entities
		for(Entity e : entities) {
			e.alert(new Description("ferry.disembark", ferry.name()));
			e.parent(dest);
		}
	}
}
