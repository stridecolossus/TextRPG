package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Optional;

import org.sarge.lib.collection.Cache;
import org.sarge.lib.collection.Pair;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Vehicle;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.world.Location;

/**
 * Helper methods for actions.
 * @author Sarge
 */
public final class ActionHelper {
    /**
     * Openable event queue.
     * @see #registerOpenableEvent(Location, Location, WorldObject, String)
     */
    public static final EventQueue QUEUE = new EventQueue();

    private static final Cache<Pair<Location, String>, Optional<Topic>> TOPICS = new Cache<>(ActionHelper::loadTopic, Cache.EvictionPolicy.NONE);
    // TODO
    // - size limited
    // - will *always* add an entry because loader returns optional!

	private ActionHelper() {
		// Utility class
	}

	/**
	 * Describes an object.
	 * @param actor		Actor
	 * @param obj		Object
	 */
	public static Description describe(Actor actor, WorldObject obj) {
		if(obj.owner() == actor) {
			return obj.describeShort();
		}
		else {
			return obj.describe();
		}
	}

	/**
	 * Validates that the given entity is a valid target for the actor.
	 * @param actor			Actor
	 * @param entity		Target entity
	 * @return Whether the given entity is a valid target
	 */
	public static boolean isValidTarget(Entity actor, Entity target) {
		final Alignment a = actor.alignment();
		final Alignment b = target.alignment();
		return (a != b) || (b == Alignment.NEUTRAL);
	}

	/**
	 * @param actor Actor
	 * @return Vehicle for the given actor or <tt>null</tt> if none
	 */
	public static Vehicle getVehicle(Actor actor) {
		final Parent parent = actor.parent();
		if(parent.parentName() == Vehicle.NAME) {
			return (Vehicle) parent;
		}
		else {
			return null;
		}
	}

	/**
	 * Finds a topic from entities in the given location.
	 * @param loc		Location
	 * @param name		Topic name
	 * @return Topic
	 */
	public static Optional<Topic> findTopic(Location loc, String name) {
	    return TOPICS.get(new Pair<>(loc, name));
	}

	/**
	 * Loader for topics cache.
	 * @param key Topic key
	 * @return Topic
	 */
	private static Optional<Topic> loadTopic(Pair<Location, String> key) {
	    final String name = key.getRight();
        return ContentsHelper.select(key.getLeft().contents().stream(), Entity.class)
            .flatMap(Entity::topics)
            .filter(t -> t.name().equals(name))
            .findFirst();
	}

	/**
	 * Kills an entity and replaces it with a corpse.
	 * @param e Entity to kill
	 * TODO - corpse expiry
	 */
	public static void kill(Entity e) {
		// Create corpse
		final Race race = e.race();
		if(race.isCorporeal()) {
			// Create corpse descriptor
			final int weight = 10; // TODO - table from ctx
			final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("corpse." + race.name())
				.weight(weight)
				.category("corpse")
				.build();

			// Create corpse and add inventory
			final Collection<Thing> inv = e.contents().stream().collect(toList());
			final WorldObject corpse = new Corpse(descriptor, e.race(), inv);
			// TODO - weight should be base-weight of race, calculated from table

			// Add corpse to current location
			final Location loc = e.location();
			corpse.setParentAncestor(loc);
		}

		// Destroy entity
		e.destroy();
	}

	/**
	 * Registers a reset event on an openable object.
	 * @param loc		This location
	 * @param other		Other location for portal events or <tt>null</tt>
	 * @param obj		Openable object
	 * @param key		Notification key
	 */
	public static void registerOpenableEvent(Location loc, Location other, WorldObject obj, String key) {
		// Create reset event
		final Openable model = obj.openableModel().get();
		final Runnable event = () -> {
			model.reset();
			final Notification notification = new Message(key, obj);
			loc.broadcast(Actor.SYSTEM, notification);
			if(other != null) {
				other.broadcast(Actor.SYSTEM, notification);
			}
		};

		// Register event
		final long reset = obj.descriptor().getProperties().getResetPeriod();
		final EventQueue.Entry entry = QUEUE.add(event, reset);

		// Note event
		model.getResetEventHolder().set(entry);
	}
}
