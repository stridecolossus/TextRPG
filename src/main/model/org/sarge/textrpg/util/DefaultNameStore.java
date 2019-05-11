package org.sarge.textrpg.util;

import java.util.Map;

/**
 * Default implementation.
 * @author Sarge
 */
public class DefaultNameStore implements NameStore {
	private final Map<String, String> names;

	/**
	 * Constructor.
	 * @param names Names indexed by key
	 */
	public DefaultNameStore(Map<String, String> names) {
		this.names = Map.copyOf(names);
	}

	@Override
	public boolean isEmpty() {
		return names.isEmpty();
	}

	@Override
	public String get(Object key) {
		return names.get(key);
	}

	@Override
	public boolean matches(String key, String name) {
		final String entry = names.get(key);
		if(entry == null) {
			return false;
		}
		else {
			return entry.equals(name);
		}
	}
}
