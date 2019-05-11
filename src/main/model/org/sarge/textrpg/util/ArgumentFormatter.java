package org.sarge.textrpg.util;

import java.util.Map;
import java.util.function.Function;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Argument formatter.
 */
@FunctionalInterface
public interface ArgumentFormatter {
	/**
	 * Formats an argument.
	 * @param arg		Argument
	 * @param store		Names store
	 * @return Formatted argument
	 */
	String format(Object arg, NameStore store);

	/**
	 * Token-replacement formatter.
	 */
	ArgumentFormatter TOKEN = (arg, store) -> store.get(arg);

	/**
	 * Plain formatter.
	 */
	ArgumentFormatter PLAIN = (arg, store) -> arg.toString();

	/**
	 * Money formatter name.
	 */
	String MONEY = "formatter.money";

	/**
	 * Numeric formatter name.
	 */
	String NUMERIC = "formatter.numeric";

	/**
	 * Creates an integer argument formatter.
	 * @param formatter Integer formatter
	 * @return Integer argument formatter
	 */
	static ArgumentFormatter integral(Function<Integer, String> formatter) {
		return (arg, store) -> formatter.apply((Integer) arg);
	}

	/**
	 * Argument formatter registry.
	 */
	class Registry extends AbstractEqualsObject {
		private final Map<String, ArgumentFormatter> formatters = new StrictMap<>();

		/**
		 * Looks up an argument formatter.
		 * @param name Name
		 * @return Argument formatter
		 */
		public ArgumentFormatter get(String name) {
			final ArgumentFormatter formatter = formatters.get(name);
			if(formatter == null) throw new IllegalArgumentException("Unknown argument formatter: " + name);
			return formatter;
		}

		/**
		 * Registers a formatter.
		 * @param name			Name
		 * @param formatter		Formatter
		 */
		public void add(String name, ArgumentFormatter formatter) {
			formatters.put(name, formatter);
		}
	}
}
