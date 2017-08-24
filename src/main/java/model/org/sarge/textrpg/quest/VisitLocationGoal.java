package org.sarge.textrpg.quest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.world.Location;

/**
 * Goal to visit one-or-more locations.
 * @author Sarge
 */
public class VisitLocationGoal implements Goal {
	private final Set<Location> locations;

	/**
	 * Constructor.
	 * @param locations Location(s) to visit
	 */
	public VisitLocationGoal(Collection<Location> locations) {
		Check.notEmpty(locations);
		this.locations = new HashSet<>(locations);
	}
	
	@Override
	public ActiveGoal start(Player player, Listener listener) {
		// Create active goal
		final Set<Object> visited = new HashSet<>();
		final ActiveGoal active = () -> new Description.Builder("goal.visit.location")
			.add("count", visited.size())
			.add("total", locations.size())
			.build();

		// Create location listener
		final Player.Listener list = loc -> {
			visited.add(loc);
			listener.update(active, visited.size() == locations.size());
			return true;
		};

		// Register listener for each location
		locations.stream().forEach(loc -> player.add(loc, list));

		return active;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
