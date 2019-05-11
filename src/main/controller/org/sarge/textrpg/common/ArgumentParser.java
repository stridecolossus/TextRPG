package org.sarge.textrpg.common;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

/**
 * An <i>argument parser</i> is used to match the next word in a command.
 * @param <T> Argument type
 * @author Sarge
 */
@FunctionalInterface
public interface ArgumentParser<T> {
	/**
	 * Parses the next command word.
	 * @param cursor Argument cursor
	 * @return Parsed argument
	 */
	T parse(WordCursor cursor);

	/**
	 * @return Number of command words consumed by this parser (default is <tt>one</tt>)
	 */
	default int count() {
		return 1;
	}

	/**
	 * Helper - Matches the next word against a set of candidate command arguments.
	 * @param cursor		Cursor
	 * @param args			Command arguments
	 * @return Matched argument
	 * @see NameStore#matches(String, String)
	 */
	static <T extends CommandArgument> T matches(WordCursor cursor, Stream<T> args) {
		final String word = cursor.next();
		final NameStore store = cursor.store();
		return args.filter(arg -> store.matches(arg.name(), word)).findAny().orElse(null);
	}

	/**
	 * Registry of argument parsers ordered by type.
	 */
	@FunctionalInterface
	interface Registry {
		/**
		 * Empty registry.
		 */
		public static final ArgumentParser.Registry EMPTY = type -> List.of();

		/**
		 * Enumerates parsers for the given type.
		 * @param type Type
		 * @return Parsers
		 */
		List<ArgumentParser<?>> parsers(Class<?> type);

		/**
		 * Helper - Creates a registry for the given parser.
		 * @param type			Type
		 * @param parser		Parser
		 * @return Registry
		 */
		static Registry of(Class<?> type, ArgumentParser<?> parser) {
			final Map<Class<?>, List<ArgumentParser<?>>> map = Map.of(type, List.of(parser));
			return t -> map.getOrDefault(t, List.of());
		}
	}
}
