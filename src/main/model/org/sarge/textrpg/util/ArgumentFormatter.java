package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notNull;

import org.springframework.stereotype.Component;

/**
 * An <i>argument formatter</i> renders a description argument.
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
	 * Default implementation that looks up the given argument string from the given name-store.
	 */
	ArgumentFormatter DEFAULT = (arg, store) -> store.get(arg);

	/**
	 * A <i>plain argument</i> is rendered as plain text using the {@link #toString()} method of the argument.
	 */
	final class PlainArgument {
		/**
		 * Formatter for a plain argument.
		 */
		@Component
		static final class PlainArgumentFormatter implements ArgumentFormatter {
			@Override
			public String format(Object arg, NameStore store) {
				return arg.toString();
			}
		}

		private final Object arg;

		/**
		 * Constructor.
		 * @param arg Argument
		 */
		public PlainArgument(Object arg) {
			this.arg = notNull(arg);
		}

		@Override
		public String toString() {
			return arg.toString();
		}
	}
}
