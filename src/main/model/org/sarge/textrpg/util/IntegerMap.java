package org.sarge.textrpg.util;

import java.util.stream.Stream;

/**
 * Map of integer values.
 * @author Sarge
 * @param <T> Key-type
 */
public interface IntegerMap<T> {
	/**
	 * Integer map entry.
	 */
	public interface Entry {
		/**
		 * @return Integer value
		 */
		int get();
	}

	/**
	 * Looks up a map entry.
	 * @param key Key
	 * @return Entry
	 */
	Entry get(T key);

	/**
	 * @return Key-set
	 */
	Stream<T> keys();
}
