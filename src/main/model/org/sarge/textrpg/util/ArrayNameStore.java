package org.sarge.textrpg.util;

import java.util.Arrays;
import java.util.Map;

/**
 * Name-store comprised of string-arrays.
 * @author Sarge
 */
public class ArrayNameStore implements NameStore {
	private final Map<String, String[]> names;

	/**
	 * Constructor.
	 * @param names Names indexed by key
	 */
	public ArrayNameStore(Map<String, String[]> names) {
		this.names = Map.copyOf(names);
	}

	@Override
	public boolean isEmpty() {
		return names.isEmpty();
	}

	@Override
	public String get(Object key) {
		final String[] array = names.get(key);
		if(array == null) {
			return null;
		}
		else {
			return array[0];
		}
	}

	@Override
	public boolean matches(String key, String name) {
		final String[] array = names.get(key);
		if(array == null) {
			return false;
		}
		else {
			return Arrays.stream(array).anyMatch(name::equals);
		}
	}
}
