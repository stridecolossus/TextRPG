package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractObject;

/**
 * A stable is used by an actor to purchase fast-travel to nearby locations.
 * @author Sarge
 */
public final class Stable extends AbstractObject {
	/**
	 *
	 * - list destinations
	 * - buy to dest -> mounted
	 * - ~ factions
	 * - cheap/normal/fast?
	 * - dismount manually
	 * - auto-follows
	 * - way-points or direction follower?
	 *
	 */

	/**
	 * Travel destination descriptor.
	 */
	public static final class Destination extends AbstractObject {
		private final String dest;
		private final Faction.Association relationship;
		private final int cost;
		private final Optional<Iterator<Exit>> follower;

		/**
		 * Constructor.
		 * @param dest				Destination name
		 * @param association		Required faction association
		 * @param cost				Purchase cost
		 * @param follower			Follower to this destination or <tt>null</tt> for fast-travel
		 */
		public Destination(String dest, Faction.Association association, int cost, Iterator<Exit> follower) {
			this.dest = notEmpty(dest);
			this.relationship = notNull(association);
			this.cost = oneOrMore(cost);
			this.follower = Optional.ofNullable(follower);
		}

		/**
		 * @return Destination name
		 */
		public String destination() {
			return dest;
		}

		/**
		 * @return Required faction relationship
		 */
		public Faction.Association relationship() {
			return relationship;
		}

		/**
		 * @return Purchase cost
		 */
		public int cost() {
			return cost;
		}

		/**
		 * @return Follower for a mounted destination or empty for fast travel
		 */
		public Optional<Iterator<Exit>> follower() {
			return follower;
		}
	}

	private final List<Destination> destinations;

	/**
	 * Constructor.
	 * @param destinations Travel destinations
	 */
	public Stable(List<Destination> destinations) {
		this.destinations = List.copyOf(destinations);
	}

	/**
	 * @return Travel destinations from this stable
	 */
	public Stream<Destination> destinations() {
		return destinations.stream();
	}
}
