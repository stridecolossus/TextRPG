package org.sarge.textrpg.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Follower;
import org.sarge.textrpg.util.Randomiser;

/**
 * Follower that selects from available exits according to a filter.
 * @author Sarge
 */
public class SelectFollower implements Follower {
	/**
	 * Selection policy when more than one direction is available.
	 */
	public enum Policy {
		/**
		 * Stops unless exactly <b>one</b> exit is available at the current location.
		 */
		ONE,
		
		/**
		 * Randomly selects from available exits.
		 */
		RANDOM
	}

	/**
	 * Creates a terrain filter.
	 * @param terrain Terrain(s) to follow
	 * @return Terrain filter
	 */
	public static Predicate<Exit> terrain(Set<Terrain> terrain) {
		return exit -> terrain.contains(exit.getDestination().getTerrain());
	}

	/**
	 * Creates a route filter.
	 * @param route Routes(s) to follow
	 * @return Route filter
	 */
	public static Predicate<Exit> route(Set<Route> route) {
		return exit -> route.contains(exit.getLink().getRoute());
	}

	private final Predicate<Exit> filter;
	private final Policy policy;
	
	private boolean retrace = true;
	private boolean bound = true;
	
	private Location prev;

	/**
	 * Constructor.
	 * @param filter	Custom filter
	 * @param policy	Selection policy
	 */
	public SelectFollower(Predicate<Exit> filter, Policy policy) {
		Check.notNull(filter);
		Check.notNull(policy);
		this.filter = filter;
		this.policy = policy;
	}

	public SelectFollower(Policy policy) {
		this(exit -> true, policy);
	}
	
	/**
	 * Sets whether this follower is bound to the current area (default is <tt>true</tt>)
	 * @param bound Whether bound to area
	 */
	public void setBound(boolean bound) {
		this.bound = bound;
	}
	
	/**
	 * Sets whether this follower prevents retracing to the previous location (default is <tt>true</tt>)
	 * @param retrace Whether to allow re-tracing
	 */
	public void setAllowRetrace(boolean retrace) {
		this.retrace = retrace;
	}

	@Override
	public Direction next(Entity actor) {
		// Enumerate available exits
		final Location loc = actor.getLocation();
		final List<Direction> dirs = new ArrayList<>();
		for(Direction dir : loc.getExits().keySet()) {
			final Exit exit = loc.getExits().get(dir);
			if(!isAvailable(exit, actor)) continue;
			if(!filter.test(exit)) continue;
			dirs.add(dir);
		}
		
		// Record visited
		prev = loc;

		// Select result
		switch(dirs.size()) {
		case 0:
			return null;
			
		case 1:
			return dirs.get(0);
			
		default:
			switch(policy) {
			case ONE:		return null;
			case RANDOM:	return Randomiser.random(dirs);
			default:		throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Tests whether the given exit is available for this follower.
	 * @param actor			Actor
	 * @param filter		Custom exit filter
	 * @return List of directions
	 */
	private boolean isAvailable(Exit exit, Entity actor) {
		// Skip invalid destinations
		final Location dest = exit.getDestination();
		if(!retrace && (dest == prev)) return false;
		if(bound && (dest.getArea() != actor.getLocation().getArea())) return false;
		
		// Skip invalid links
		final Link link = exit.getLink();
		if(!link.isTraversable(actor)) return false;
		if(!exit.perceivedBy(actor)) return false;
		
		// Valid link
		return true;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
