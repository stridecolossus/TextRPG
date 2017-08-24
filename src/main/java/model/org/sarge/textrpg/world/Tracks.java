package org.sarge.textrpg.world;

import org.sarge.lib.object.EqualsBuilder;
import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.util.Percentile;

/**
 * Sets of tracks.
 * @author Sarge
 */
public final class Tracks {
	private final String name;
	private final Location loc;
	private final Direction dir;
	private final Percentile vis;
	private final long time;
	
	/**
	 * Constructor.
	 * @param name		Name of the race that generated this set of tracks
	 * @param loc		Location
	 * @param dir		Direction of tracks
	 * @param vis		Visibility of this set of tracks
	 * @param time		Creation time
	 */
	public Tracks(String name, Location loc, Direction dir, Percentile vis, long time) {
		Check.notEmpty(name);
		Check.notNull(loc);
		Check.notNull(dir);
		Check.notNull(vis);
		Check.zeroOrMore(time);
		this.name = name;
		this.loc = loc;
		this.dir = dir;
		this.vis = vis;
		this.time = time;
	}

	/**
	 * @return Name of the race that generated this set of tracks
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return Tracks location
	 */
	public Location getLocation() {
		return loc;
	}
	
	/**
	 * @return Direction of this set of tracks
	 */
	public Direction getDirection() {
		return dir;
	}

	/**
	 * @return Visibility of these tracks
	 */
	public Percentile getVisibility() {
		return vis;
	}
	
	/**
	 * @return Creation time of this set of tracks
	 */
	public long getCreationTime() {
		return time;
	}
	
	public void remove() {
		loc.remove(this);
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.equals(this, that);
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
