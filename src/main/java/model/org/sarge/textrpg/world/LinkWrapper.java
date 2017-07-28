package org.sarge.textrpg.world;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Transient link wrapper.
 * @author Sarge
 */
public final class LinkWrapper {
	/**
	 * Reverse link policy.
	 */
	public enum ReversePolicy {
		/**
		 * One-way link.
		 */
		ONE_WAY,
		
		/**
		 * Reverse is a simple link.
		 */
		SIMPLE,
		
		/**
		 * Reverse is the inverse of this link.
		 */
		INVERSE
	}

	private final Direction dir;
	private final Link link;
	private final Location dest;
	private final Direction reverse;
	private final ReversePolicy policy;
	
	/**
	 * Constructor.
	 * @param dir			Direction
	 * @param link			Link descriptor
	 * @param dest			Destination
	 * @param reverse		Reverse direction
	 * @param policy		Reverse policy
	 */
	public LinkWrapper(Direction dir, Link link, Location dest, Direction reverse, ReversePolicy policy) {
		Check.notNull(dir);
		Check.notNull(link);
		Check.notNull(dest);
		Check.notNull(reverse);
		this.dir = dir;
		this.link = link;
		this.dest = dest;
		this.reverse = reverse;
		this.policy = policy;
	}
	
	/**
	 * Constructor for a {@link ReversePolicy#SIMPLE} link.
	 * @param dir		Direction
	 * @param link		Link descriptor
	 * @param dest		Destination
	 */
	public LinkWrapper(Direction dir, Link link, Location dest) {
		this(dir, link, dest, dir.reverse(), ReversePolicy.SIMPLE);
	}
	
	/**
	 * @return Link direction
	 */
	public Direction getDirection() {
		return dir;
	}

	/**
	 * @return Link descriptor
	 */
	public Link getLink() {
		return link;
	}
	
	/**
	 * @return Destination
	 */
	public Location getDestination() {
		return dest;
	}

	/**
	 * @return Reverse link policy
	 */
	public ReversePolicy getReversePolicy() {
		return policy;
	}
	
	/**
	 * @return Reverse link direction
	 */
	public Direction getReverseDirection() {
		return reverse;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
