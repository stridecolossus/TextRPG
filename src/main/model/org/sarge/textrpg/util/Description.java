package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Description of an object or entity.
 * <p>
 * A description is comprised of:
 * <ul>
 * <li>A <i>key</i> used to lookup the description template from a name-store</li>
 * <li>A set of none-or-more arguments that are injected into the description</li>
 * <li>A formatter for each argument that specifies how that argument is rendered</li>
 * </ul>
 * @see ArgumentFormatter
 * @author Sarge
 */
public final class Description extends AbstractEqualsObject {
	/**
	 * Default argument identifier.
	 */
	private static final String NAME = "name";

	/**
	 * Cache of simple descriptions.
	 */
	private static final Map<String, Description> CACHE = new ConcurrentHashMap<>();

	/**
	 * Marker instance to indicate that the current location should be rendered.
	 */
	public static final Description DISPLAY_LOCATION = new Description("display.location");

	/**
	 * Description argument entry.
	 */
	public static class Entry extends AbstractEqualsObject {
		private final Object arg;
		private final ArgumentFormatter formatter;

		/**
		 * Constructor.
		 * @param arg			Argument
		 * @param formatter		Formatter
		 */
		private Entry(Object arg, ArgumentFormatter formatter) {
			this.arg = notNull(arg);
			this.formatter = notNull(formatter);
		}

		/**
		 * @return Formatter for this argument
		 */
		public ArgumentFormatter formatter() {
			return formatter;
		}

		/**
		 * @return Argument
		 */
		public Object argument() {
			return arg;
		}
	}

	/**
	 * Creates a cached description without arguments.
	 * @param key Description key
	 * @return Description
	 */
	public static Description of(String key) {
		return CACHE.computeIfAbsent(key, Description::new);
	}

	private final String key;
	private final Map<String, Entry> args;

	/**
	 * Constructor.
	 * @param key		Formatting key
	 * @param args		Arguments
	 */
	private Description(String key, Map<String, Entry> args) {
		this.key = notEmpty(key);
		this.args = Map.copyOf(args);
	}

	/**
	 * Convenience constructor for a description with a single token-replacement argument.
	 * @param key 		Formatting key
	 * @param name		Argument name
	 * @param arg 		Argument
	 */
	public Description(String key, String name, Object value) {
		this(key, Map.of(name, new Entry(value, ArgumentFormatter.TOKEN)));
	}

	/**
	 * Convenience constructor for a description with a single <i>name</i> argument.
	 * @param key Formatting key
	 * @param arg Argument
	 */
	public Description(String key, String arg) {
		this(key, NAME, arg);
	}

	/**
	 * Default constructor for a simple description without arguments.
	 * @param key Description key
	 */
	public Description(String key) {
		this(key, Map.of());
	}

	/**
	 * @return Formatting key
	 */
	public String key() {
		return key;
	}

	/**
	 * Looks up an argument entry.
	 * @param name Argument identifier
	 * @return Argument entry or <tt>null</tt> if not present
	 */
	public Entry get(String name) {
		return args.get(name);
	}

	/**
	 * Builder for a description.
	 */
	public static class Builder {
		private final String key;
		private final Map<String, Entry> args = new StrictMap<>();

		/**
		 * Constructor.
		 * @param key Formatting key
		 */
		public Builder(String key) {
			this.key = key;
		}

		/**
		 * Looks up an argument value.
		 * @param name Argument name
		 * @return Value or <tt>null</tt> if not present
		 */
		public Entry get(String name) {
			return args.get(name);
		}

		/**
		 * Adds a description argument with a specified formatter.
		 * @param name			Name
		 * @param value			Value
		 * @param formatter		Formatter
		 */
		public Builder add(String name, Object value, ArgumentFormatter formatter) {
			args.put(name, new Entry(value, formatter));
			return this;
		}

		/**
		 * Adds a token-replacement description argument.
		 * @param name		Name
		 * @param value		Value
		 * @see ArgumentFormatter#TOKEN
		 */
		public Builder add(String name, String value) {
			add(name, value, ArgumentFormatter.TOKEN);
			return this;
		}

		/**
		 * Adds a <i>name</i> argument.
		 * @param name Name
		 */
		public Builder name(String name) {
			add(NAME, name);
			return this;
		}

		/**
		 * Adds an integer argument rendered as a plain value.
		 * @param name		Name
		 * @param value		Integer value
		 * @see ArgumentFormatter#PLAIN
		 */
		public Builder add(String name, int value) {
			add(name, value, ArgumentFormatter.PLAIN);
			return this;
		}

		/**
		 * Adds an enumeration constant argument.
		 * @param name		Name
		 * @param value		Enumeration constant
		 * @see TextHelper#prefix(Enum)
		 */
		public <E extends Enum<E>> Builder add(String name, E value) {
			add(name, TextHelper.prefix(value), ArgumentFormatter.TOKEN);
			return this;
		}

		/**
		 * Constructs this description.
		 * @return New description
		 */
		public Description build() {
			return new Description(key, args);
		}
	}
}
