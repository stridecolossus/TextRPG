package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * A <i>description</i> is the textual representation of a location, object or entity comprising:
 * <ul>
 * <li>The description <i>key</i> used to lookup the description template from the {@link NameStore}</li>
 * <li>A set of none-or-more arguments that are injected into the template</li>
 * </ul>
 * @see DescriptionFormatter
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
	 * Creates a cached description without arguments.
	 * @param key Description key
	 * @return Description
	 */
	public static Description of(String key) {
		return CACHE.computeIfAbsent(key, Description::new);
	}

	private final String key;
	private final Map<String, Object> args;

	/**
	 * Constructor.
	 * @param key		Formatting key
	 * @param args		Arguments
	 */
	private Description(String key, Map<String, Object> args) {
		this.key = notEmpty(key);
		this.args = Map.copyOf(args);
	}

	/**
	 * Convenience constructor for a description with a single argument.
	 * @param key 		Formatting key
	 * @param name		Argument name
	 * @param arg 		Argument
	 */
	public Description(String key, String name, Object value) {
		this(key, Map.of(name, value));
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
	public Object get(String name) {
		return args.get(name);
	}

	/**
	 * Builder for a description.
	 */
	public static class Builder {
		private final String key;
		private final Map<String, Object> args = new StrictMap<>();

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
		Object get(String name) {
			return args.get(name);
		}

		/**
		 * Adds a description argument.
		 * @param name			Name
		 * @param value			Value
		 */
		public Builder add(String name, Object value) {
			args.put(name, value);
			return this;
		}

		/**
		 * Helper - Adds a <i>name</i> argument.
		 * @param name Name
		 */
		public Builder name(String name) {
			add(NAME, name);
			return this;
		}

		/**
		 * Helper - Adds an enumeration constant argument.
		 * @param name		Name
		 * @param value		Enumeration constant
		 * @see TextHelper#prefix(Enum)
		 */
		public <E extends Enum<E>> Builder add(String name, E value) {
			add(name, TextHelper.prefix(value));
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
