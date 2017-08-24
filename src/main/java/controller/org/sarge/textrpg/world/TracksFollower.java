package org.sarge.textrpg.world;

import java.util.Comparator;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Follower;

/**
 * Tracks follower.
 * @author Sarge
 * @see Tracks
 */
public class TracksFollower implements Follower {
	/**
	 * Orders tracks by freshness.
	 */
	private static final Comparator<Tracks> COMPARATOR = Comparator.comparing(Tracks::getCreationTime);
	
	private final String name;
	private final int level;

	/**
	 * Constructor.
	 * @param name		Name of tracks to follow
	 * @param level		Tracking level
	 */
	public TracksFollower(String name, int level) {
		Check.notEmpty(name);
		Check.oneOrMore(level);
		this.name = name;
		this.level = level;
	}

	@Override
	public Direction next(Entity actor) {
		/*
		return loc.getContents().stream()
			.filter(t -> t instanceof Tracks)
			.map(t -> (Tracks) t)
			.filter(t -> t.getName().equals(name))
			.filter(t -> t.getVisibility().invert().intValue() < level)
			.max(COMPARATOR)
			.map(Tracks::getDirection)
			.orElse(null);
			*/
		return null;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
