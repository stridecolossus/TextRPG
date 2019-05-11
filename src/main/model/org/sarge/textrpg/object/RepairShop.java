package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.util.ActionException;

/**
 * Repair facility.
 * @author Sarge
 */
public class RepairShop {
	private final Set<String> cats;
	private final Map<Actor, Collection<WorldObject>> repaired = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 * @param cats Object categories that can be repaired by this shop
	 */
	public RepairShop(Set<String> cats) {
		this.cats = Set.copyOf(cats);
	}

	/**
	 * Repairs the given object.
	 * @param actor		Actor
	 * @param obj 		Object to repair
	 * @param queue		Queue for repair events
	 * @throws ActionException if the object is not damaged or cannot be repaired by this shop
	 */
	public void repair(Actor actor, DurableObject obj, RepairController controller) throws ActionException {
		// Check can be repaired
		cleanup();
		if(!obj.descriptor().characteristics().categories().stream().anyMatch(cats::contains)) throw ActionException.of("repair.cannot.repair");
		if(!obj.isDamaged()) throw ActionException.of("repair.not.damaged");

		// Remove from inventory
		obj.destroy();

		// Lookup or create pending repaired objects for this actor
		final Collection<WorldObject> pending = repaired.computeIfAbsent(actor, key -> new HashSet<>());

		// Start repair
		controller.repair(obj, pending);
	}

	/**
	 * Retrieves repaired objects belonging to the given actor.
	 * @param actor Actor
	 * @return Repaired objects
	 */
	public Stream<WorldObject> repaired(Actor actor) {
		cleanup();
		final var objects = repaired.get(actor);
		if(objects == null) {
			// No pending objects
			return Stream.empty();
		}
		else {
			// Remove repaired objects for this actor
			repaired.remove(actor);
			return objects.stream();
		}
	}

	/**
	 * Clears unclaimed pending stores.
	 */
	private void cleanup() {
		synchronized(repaired) {
			final var empty = repaired.entrySet().stream().filter(e -> e.getValue().isEmpty()).map(Map.Entry::getKey).collect(toList());
			empty.forEach(repaired::remove);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("repaired", repaired.size()).toString();
	}
}
