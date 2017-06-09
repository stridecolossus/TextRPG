package org.sarge.textrpg.entity;

import org.sarge.textrpg.world.Direction;

/**
 * Defines something that an entity can follow such as a route, tracks, etc.
 * @author Sarge
 */
public interface Follower {
	/**
	 * Determines the next direction to follow.
	 * @param actor Actor
	 * @return Next direction to follow or <tt>null</tt> if not available
	 */
	Direction next(Entity actor);
}
