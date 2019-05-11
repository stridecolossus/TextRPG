package org.sarge.textrpg.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictSet;
import org.sarge.textrpg.entity.Entity.LocationTrigger;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.MovementController;
import org.springframework.stereotype.Controller;

/**
 * Controller for location triggers.
 * @author Sarge
 */
@Controller
public class LocationTriggerController implements MovementController.Listener {
	private final Map<Area, Map<Location, Set<LocationTrigger>>> areas = new ConcurrentHashMap<>();

	// TODO - could re-use this for visit location quests?

	/**
	 * Finds location triggers in the given location.
	 * @param loc Location
	 * @return Triggers
	 */
	public Stream<LocationTrigger> find(Location loc) {
		return areas
			.getOrDefault(loc.area(), Map.of())
			.getOrDefault(loc, Set.of())
			.stream();
	}

	/**
	 * Registers a location trigger in the given location.
	 * @param loc			Location
	 * @param trigger		Trigger
	 */
	public void add(Location loc, LocationTrigger trigger) {
		areas
			.computeIfAbsent(loc.area(), key -> new HashMap<>())
			.computeIfAbsent(loc, key -> new StrictSet<>())
			.add(trigger);
	}

	/**
	 * Removes a location trigger.
	 * @param loc			Location
	 * @param trigger		Trigger
	 */
	public void remove(Location loc, LocationTrigger trigger) {
		areas
			.get(loc.area())
			.get(loc)
			.remove(trigger);
	}

	@Override
	public void update(Entity actor, Exit exit, Location prev) {
		find(actor.location()).forEach(t -> t.trigger(actor));
	}
}
