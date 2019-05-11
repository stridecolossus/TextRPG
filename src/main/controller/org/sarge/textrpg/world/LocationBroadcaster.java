package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.oneOrMore;

import java.util.HashSet;
import java.util.Set;

/**
 * A <i>location broadcaster</i> applies a {@link Visitor} to neighbouring locations.
 * @author Sarge
 */
public class LocationBroadcaster {
	/**
	 * Exit visitor.
	 */
	public interface Visitor {
		/**
		 * Visits an exit.
		 * @param exit		Exit
		 * @param depth		Depth
		 */
		void visit(Exit exit, int depth);
	}

	private final int max;

	/**
	 * Constructor.
	 * @param max Maximum traversal depth
	 */
	public LocationBroadcaster(int max) {
		this.max = oneOrMore(max);
	}

	/**
	 * @return Maximum traversal depth
	 */
	public int max() {
		return max;
	}

	/**
	 * Visits the neighbours of the given location.
	 * @param start Start location
	 */
	public void visit(Location start, Visitor visitor) {
		// Traversal instance
		class Instance {
			private final Set<Location> visited = new HashSet<>();

			/**
			 * Visits the neighbours of the given location.
			 * @param loc			Location
			 * @param depth			Depth
			 */
			private void visit(Location loc, int depth) {
				loc.exits().stream()
					.filter(exit -> !visited.contains(exit.destination()))
					.forEach(exit -> visit(exit, depth));
			}

			/**
			 * Visits the destination of the given exit and recurses to its neighbours.
			 * @param exit			Exit
			 * @param depth			Depth
			 */
			private void visit(Exit exit, int depth) {
				// Visit location
				final Location loc = exit.destination();
				visited.add(loc);
				visitor.visit(exit, depth);

				// Recurse to neighbours
				if(depth < max) {
					visit(loc, depth + 1);
				}
			}
		}

		// Traverse neighbours
		final Instance instance = new Instance();
		instance.visited.add(start);
		instance.visit(start, 1);
	}
}
