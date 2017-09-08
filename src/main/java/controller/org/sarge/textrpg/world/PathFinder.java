package org.sarge.textrpg.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.DataTableCalculator;

/**
 * Constructs a path between two locations using an A* based algorithm.
 * @author chris
 * TODO
 * - heuristic multiplier?
 * - default capacity for open/closed sets?
 */
public class PathFinder {
	private final DataTableCalculator move;

	private int max = Integer.MAX_VALUE;

	/**
	 * Constructor.
	 * @param move Movement cost calculator
	 */
	public PathFinder(DataTableCalculator move) {
		this.move = Check.notNull(move);
	}

	/**
	 * Sets the maximum <i>depth</i> of links to traverse.
	 * @param max Maximum link depth
	 */
	public void setMaxDepth(int max) {
		this.max = Check.oneOrMore(max);
	}

	/**
	 * Candidate node in the path.
	 */
	private class Node implements Comparable<Node> {
		private final Location loc;
		private Direction dir;
		private Node parent;
		private float score;
		private int depth;

		/**
		 * Constructor.
		 * @param loc Location
		 */
		public Node(Location loc) {
			this.loc = loc;
		}

		/**
		 * Updates the score and pointer to this node.
		 * @param parent		Parent node
		 * @param dir			Direction to this node
		 * @param score			Movement cost
		 */
		public void update(Node parent, Direction dir, float score) {
			this.parent = parent;
			this.dir = dir;
			this.score = score;
			this.depth = parent.depth + 1;
		}

		@Override
		public int compareTo(Node that) {
			return (int) (this.score - that.score);
		}
	}

	/**
	 * Finds the best path between the given locations for the given actor.
	 * @param start		Start location
	 * @param end		Target
	 * @param actor		Actor
	 * @return Path
	 */
	public Optional<Iterator<Direction>> build(Location start, Location end, Actor actor) {
		Check.notNull(start);
		Check.notNull(end);
		if(start == end) throw new IllegalArgumentException("Start and target are the same");

		// Init
		final TreeSet<Node> open = new TreeSet<>();
		final Collection<Location> closed = new HashSet<>();
		open.add(new Node(start));
		Node found = null;

		// Expand candidate nodes until target reached
		while(true) {
			// Stop if no more available candidates
			if(open.isEmpty()) return Optional.empty();

			// Select lowest scoring node for next iteration
			final Node next = open.first();
			open.remove(next);
			closed.add(next.loc);

			// Stop if reached maximum traversal depth
			if(next.depth >= max) continue;

			// Create new nodes for all available links from this location
			for(final Entry<Direction, Exit> exit : next.loc.getExits().entrySet()) {
				// Skip links that cannot be traversed
				final Link link = exit.getValue().getLink();
				if(!link.isTraversable(actor)) continue;
				if(!link.isVisible(actor)) continue;

				// Skip if already discounted
				final Location dest = exit.getValue().getDestination();
				if(closed.contains(dest)) continue;

				// Add new candidate node
				final Node node = new Node(dest);
				// TODO - heuristic? number of links traversed?
				final float score = move.multiply(dest.getTerrain(), link.route(), Stance.DEFAULT);
				node.update(next, exit.getKey(), score);
				open.add(node);

				// Check whether reached target (with a better score)
				if(dest == end) {
					if((found == null) || (node.score < found.score)) {
						found = node;
					}
				}

				// TODO - check better path, how?
			}

			// Stop if reached target
			if(found != null) {
				// Build path
				final List<Direction> path = build(start, found);
				return Optional.of(path.iterator());
			}
		}
	}

	/**
	 * Builds a path from the start location.
	 * @param start		Start location
	 * @param end		End node
	 * @return Path
	 */
	private static List<Direction> build(Location start, Node end) {
		final List<Direction> path = new ArrayList<>();
		Node node = end;
		while(true) {
			path.add(node.dir);
			node = node.parent;
			if(node.loc == start) break;
		}
		Collections.reverse(path);
		return path;
	}
}
