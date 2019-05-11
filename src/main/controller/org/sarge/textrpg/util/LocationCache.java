package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.sarge.textrpg.world.Location;

/**
 * Cache for objects indexed by location.
 * @author Sarge
 * @param <T> Data-type
 */
public class LocationCache<T> {
	private final Map<Location, Optional<T>> map = new HashMap<>();

	private final Function<Location, Optional<T>> mapper;

	/**
	 * Constructor.
	 * @param mapper Looks-up an object in the given location
	 */
	public LocationCache(Function<Location, Optional<T>> mapper) {
		this.mapper = notNull(mapper);
	}

	/**
	 * Looks-up an object in the given location.
	 * @param loc Location to search
	 * @return Result
	 */
	public Optional<T> find(Location loc) {
		final Optional<T> prev = map.get(loc);
		if(prev == null) {
			// Delegate
			final Optional<T> value = mapper.apply(loc);
			if(value.isPresent()) {
				// New entry
				map.put(loc, value);
				return value;
			}
			else {
				// Not found
				return value;
			}
		}
		else {
			// Found cached entry
			return prev;
		}
	}
}
