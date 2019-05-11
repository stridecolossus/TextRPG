package org.sarge.textrpg.entity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.contents.TrackedContents;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.WorldObject;

/**
 * Character inventory.
 * @author Sarge
 */
public class Inventory extends TrackedContents {
	/**
	 * Empty inventory.
	 */
	public static final Inventory EMPTY = new Inventory() {
		@Override
		protected void add(Thing thing) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void remove(Thing thing) {
			throw new UnsupportedOperationException();
		}
	};

	private final Equipment equipment = new Equipment();
	private final Set<Container> containers = new HashSet<>();

	@Override
	public EnumerationPolicy policy() {
		return EnumerationPolicy.NONE;
	}

	/**
	 * Recursively finds a matching object in this inventory.
	 * @param filter Filter
	 * @return Matched object
	 */
	public Optional<WorldObject> find(Predicate<WorldObject> filter) {
		final var children = containers.stream().map(Container::contents).flatMap(Contents::stream);
		final var combined = Stream.concat(stream(), children);
		return StreamUtil.select(WorldObject.class, combined).filter(filter).findAny();
	}

	/**
	 * @return Equipment model
	 */
	public Equipment equipment() {
		return equipment;
	}

	/**
	 * Finds a suitable container for the given object.
	 * @param obj Object
	 * @return Matching container
	 */
	public Optional<Container> container(WorldObject obj) {
		final Predicate<Container> matches = c -> !c.contents().reason(obj).isPresent();
		return containers.stream().filter(matches).findAny();
	}

	@Override
	protected void add(Thing thing) {
		super.add(thing);
		if(thing instanceof Container) {
			containers.add((Container) thing);
		}
	}

	@Override
	protected void remove(Thing thing) {
		super.remove(thing);
		containers.remove(thing);
	}
}
