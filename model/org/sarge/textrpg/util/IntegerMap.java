package org.sarge.textrpg.util;

/**
 * Map of integer values indexed by an enumeration.
 * @author Sarge
 * @param <E> Enumeration
 */
public interface IntegerMap<E extends Enum<E>> {
	/**
	 * Retrieves an integer from this map.
	 * @param key Enumeration key
	 * @return Integer value
	 */
	int get(E key);
}
