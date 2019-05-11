package org.sarge.textrpg.object;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.sarge.textrpg.common.Actor;

/**
 * A <i>loot<i> factory generates objects for treasure, corpses, etc.
 * @author Sarge
 * TODO - relevant modifiers injected into generate(), e.g. luck, race, etc? maybe some sort of contextual wrapper to abstract?
 */
@FunctionalInterface
public interface LootFactory {
	/**
	 * Generates loot for the given actor.
	 * @param actor Actor
	 * @return Loot
	 */
	Stream<WorldObject> generate(Actor actor);

	/**
	 * Empty loot factory.
	 */
	LootFactory EMPTY = actor -> Stream.empty();

	/**
	 * Factory for a specific type of object.
	 * @param descriptor Object descriptor
	 * @return Object loot-factory
	 */
	static LootFactory of(ObjectDescriptor descriptor, int num) {
		return actor -> IntStream.range(0, num).mapToObj(n -> descriptor.create());
	}

	/**
	 * Factory for {@link Money}.
	 * @param amount Amount of money
	 * @return Money loot-factory
	 */
	static LootFactory money(int amount) {
		return actor -> Stream.of(new Money(amount));
	}
}
