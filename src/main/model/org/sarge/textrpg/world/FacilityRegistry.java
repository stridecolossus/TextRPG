package org.sarge.textrpg.world;

import java.util.Map;
import java.util.Optional;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractObject;
import org.springframework.stereotype.Service;

/**
 * The <i>facility registry</i> is used to find static facilities in a location.
 * @author Sarge
 */
@Service
public class FacilityRegistry extends AbstractObject {
	private final Map<Location, Object> facilities = new StrictMap<>();

	/**
	 * Finds a facility of the given type.
	 * @param loc		Location
	 * @param type		Facility type
	 * @return Facility
	 */
	public <T> Optional<T> find(Location loc, Class<T> type) {
		return Optional.ofNullable(facilities.get(loc)).filter(f -> type.isAssignableFrom(f.getClass())).map(type::cast);
	}

	/**
	 * Registers a facility in the given location.
	 * @param loc			Location
	 * @param facility		Facility
	 * @throws IllegalArgumentException if the location already contains a facility
	 */
	public void add(Location loc, Object facility) {
		facilities.put(loc, facility);
	}
}
