package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.collection.Pair;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.EventQueue;

/**
 * Shop model.
 * @author Sarge
 */
public class RepairShop {
	private final long duration;
	private final int mod;
	private final long discard;

	private final List<Pair<Actor, WorldObject>> repaired = new ArrayList<>();
	private final EventQueue queue = new EventQueue();

	/**
	 * Constructor.
	 * @param duration		Repair duration multiplier (ms)
	 * @param mod			Repair cost multiplier
	 * @param discard		Discard period (ms)
	 */
	public RepairShop(long duration, int mod, long discard) {
		Check.oneOrMore(duration);
		Check.oneOrMore(mod);
		Check.oneOrMore(discard);
		this.duration = duration;
		this.mod = mod;
		this.discard = discard;
	}

	/**
	 * @return Shop repair queue
	 */
	public EventQueue getEventQueue() {
		return queue;
	}

	/**
	 * Calculates the cost of repairing the given object.
	 * @param obj Damaged object
	 * @return Repair cost
	 */
	public int calculateRepairCost(WorldObject obj) {
		if(obj instanceof DurableObject) {
			final DurableObject durable = (DurableObject) obj;
			return durable.getWear() * mod;
		}
		else {
			return 0;
		}
	}

	/**
	 * Repairs a damaged object.
	 * @param obj		Damaged object
	 * @param actor		Actor
	 * @return Repair duration (ms)
	 * @throws ActionException
	 */
	public long repair(Actor actor, WorldObject obj) throws ActionException {
		// Check requires repair
		final int cost = calculateRepairCost(obj);
		if(cost == 0) throw new ActionException("repair.not.damaged");

		// Remove from inventory
		obj.destroy();

		// Generate repair event
		final Pair<Actor, WorldObject> entry = new Pair<>(actor, obj);
		final Runnable event = () -> repaired.add(entry);
		final long duration = this.duration * cost;
		queue.add(event, duration);

		// Generate discard event
		final Runnable discardEvent = () -> repaired.stream().filter(e -> e == entry).findFirst().ifPresent(repaired::remove);
		queue.add(discardEvent, discard + duration);

		return duration;
	}

	/**
	 * Returns repaired objects belonging to the given actor.
	 * @param actor Actor
	 * @return Repaired objects
	 * TODO - how to make this automatic when entity enters a location?
	 */
	public Stream<WorldObject> getRepaired(Actor actor) {
		final List<Pair<Actor, WorldObject>> results = repaired.stream().filter(entry -> entry.getLeft() == actor).collect(toList());
		repaired.removeAll(results);
		return results.stream().map(Pair::getRight);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
