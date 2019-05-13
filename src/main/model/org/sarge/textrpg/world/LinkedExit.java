package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Transient descriptor for a location exit that is patched during the linkage phase.
 * @see Location.Linker
 */
public class LinkedExit extends AbstractEqualsObject {
	/**
	 * Reverse exit policy.
	 */
	public enum ReversePolicy {
		/**
		 * Reverse exit is the <i>inverse</i> of this exit.
		 * @see Link#invert()
		 */
		INVERSE,

		/**
		 * Reverse exit has a {@link Link#DEFAULT} descriptor.
		 */
		SIMPLE,

		/**
		 * Exit is uni-directional.
		 */
		ONE_WAY
	}

	private final Location start;
	private final Direction dir;
	private final Link link;
	private final String dest;
	private final LinkedExit.ReversePolicy policy;
	private final Direction reverse;

	/**
	 * Constructor.
	 * @param start			Starting location
	 * @param dir			Direction
	 * @param link			Link descriptor
	 * @param dest			Destination location name
	 * @param policy		Reverse link policy
	 * @param reverse		Optional over-ridden reverse link direction
	 * @throws IllegalArgumentException if a reverse direction is provided for a one-way exit
	 */
	public LinkedExit(Location start, Direction dir, Link link, String dest, LinkedExit.ReversePolicy policy, Direction reverse) {
		this.start = notNull(start);
		this.dir = notNull(dir);
		this.link = notNull(link);
		this.dest = notEmpty(dest);
		this.policy = notNull(policy);
		this.reverse = reverse;
		if((policy == LinkedExit.ReversePolicy.ONE_WAY) && Objects.nonNull(reverse)) throw new IllegalArgumentException("Illogical reverse direction for one-way exit");
	}

	/**
	 * @return Starting location
	 */
	public Location start() {
		return start;
	}

	/**
	 * @return Destination location name
	 */
	public String destination() {
		return dest;
	}

	/**
	 * @return Reverse exit policy
	 */
	public LinkedExit.ReversePolicy policy() {
		return policy;
	}

	/**
	 * Creates the exit.
	 * @param dest Destination location
	 * @return Exit
	 */
	public Exit exit(Location dest) {
		return Exit.of(dir, link, dest);
	}

	/**
	 * Creates the reverse exit.
	 * @return Reverse exit
	 * @throws IllegalStateException if this exit is one-way
	 */
	public Exit reverse() {
		if(policy == LinkedExit.ReversePolicy.ONE_WAY) throw new IllegalStateException("Cannot reverse a one-way exit: " + this);
		final Direction reverse = reverseDirection();
		final Link inverted = invert();
		return Exit.of(reverse, inverted, start);
	}

	/**
	 * Inverts the link descriptor.
	 * @return Inverted link
	 */
	private Link invert() {
		switch(policy) {
		case INVERSE:		return link.invert();
		case SIMPLE:		return Link.DEFAULT;
		default:			throw new RuntimeException();
		}
	}

	/**
	 * @return Reverse direction
	 */
	private Direction reverseDirection() {
		if(reverse == null) {
			return dir.reverse();
		}
		else {
			return reverse;
		}
	}
}