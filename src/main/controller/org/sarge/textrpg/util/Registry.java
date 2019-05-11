package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.Map;
import java.util.function.Function;

import org.sarge.lib.collection.StrictMap;

/**
 * A <i>registry</i> is a table of objects indexed by name.
 * @author Sarge
 * @param <T> Registry type
 */
public interface Registry<T> {

	/**
	 * Looks-up an entry.
	 * @param name Name
	 * @return Entry or <tt>null</tt> if not present
	 */
	T get(String name);

	/**
	 * Builder for a registry.
	 * @param <T> Registry type
	 */
	class Builder<T> implements Registry<T> {
		private final Map<String, T> registry = new StrictMap<>();
		private final Function<T, String> mapper;

		/**
		 * Constructor.
		 * @param mapper Extracts the entry name
		 */
		public Builder(Function<T, String> mapper) {
			this.mapper = notNull(mapper);
		}

		@Override
		public T get(String name) {
			return registry.get(name);
		}

		/**
		 * Adds an entry.
		 * @param entry Entry to add
		 * @throws IllegalArgumentException if the name or value are <tt>null</tt> or the entry is a duplicate
		 */
		public Builder<T> add(T entry) {
			registry.put(mapper.apply(entry), entry);
			return this;
		}

		/**
		 * Constructs this registry.
		 * @return New registry
		 */
		public Registry<T> build() {
			return new Registry<>() {
				@Override
				public T get(String name) {
					final T value = registry.get(name);
					if(value == null) throw new IllegalArgumentException("Unknown registry entry: " + name);
					return value;
				}

				@Override
				public String toString() {
					return registry.toString();
				}
			};
		}
	}
}
