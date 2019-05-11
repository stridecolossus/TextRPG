package org.sarge.textrpg.entity;

import java.util.Collection;
import java.util.stream.Stream;

import org.sarge.textrpg.common.CommandArgument;

/**
 * A command argument <i>factory</i> generates candidate action arguments for a given actor.
 * @author Sarge
 */
public interface CommandArgumentFactory<T extends CommandArgument> {
	/**
	 * Generates candidate command arguments for the given actor.
	 * @param actor Actor
	 * @return Command arguments
	 */
	Stream<? extends T> stream(Entity actor);

	/**
	 * Factory that generates empty results.
	 */
	CommandArgumentFactory<?> EMPTY = actor -> Stream.empty();

	/**
	 * Creates a factory for a single argument.
	 * @param arg Argument
	 * @return Literal factory
	 */
	static CommandArgumentFactory<?> of(CommandArgument arg) {
		return actor -> Stream.of(arg);
	}

	/**
	 * Creates a compound argument factory.
	 * @param factories Factories
	 * @return Compound factory
	 */
	static <T extends CommandArgument> CommandArgumentFactory<T> compound(Collection<CommandArgumentFactory<? extends T>> factories) {
		return actor -> factories.stream().flatMap(f -> f.stream(actor));
	}
}
