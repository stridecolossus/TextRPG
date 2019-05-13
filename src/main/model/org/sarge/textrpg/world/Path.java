package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.world.ExitMap.MutableExitMap;

import com.sun.jdi.connect.Connector;

/**
 * A <i>path</i> is a linear, one-dimensional list of locations that can be traversed start-to-end in either direction.
 * <p>
 * Paths can be used to model areas such as tunnels, interconnecting trails in a forest, mountain passes, etc.
 * <p>
 * Notes:
 * <ul>
 * <li>All locations in the path share the same area and link descriptor</li>
 * <li>The start and end of the path are automatically exposed as connectors</li>
 * <li>Paths can also contain <i>junctions</i> which are pre-existing locations, see {@link Path.Builder#junction(Direction, Connector)}</li>
 * <li>A path must contain at least <b>two</b> locations</li>
 * </ul>
 * @author Sarge
 */
public class Path extends AbstractObject {
	/**
	 * Custom exits implementation for a path location.
	 */
	private static class PathExits extends AbstractEqualsObject implements ExitMap {
		private final Exit next, prev;

		/**
		 * Constructor.
		 * @param exits Exits
		 */
		private PathExits(ExitMap exits) {
			final Iterator<Exit> itr = exits.stream().iterator();
			next = itr.next();
			prev = itr.next();
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Optional<Exit> find(Direction dir) {
			if(dir == next.direction()) {
				return Optional.of(next);
			}
			else
			if(dir == prev.direction()) {
				return Optional.of(prev);
			}
			else {
				return ExitMap.EMPTY_EXIT;
			}
		}

		@Override
		public Stream<Exit> stream() {
			return Stream.of(next, prev);
		}
	}

	/**
	 * Intermediate location in this path.
	 */
	private static class PathLocation extends Location {
		private final Area area;

		/**
		 * Constructor.
		 * @param descriptor 	Location descriptor
		 * @param area			Area
		 */
		private PathLocation(Descriptor descriptor, Area area) {
			super(descriptor, new MutableExitMap());
			this.area = notNull(area);
		}

		@Override
		public Area area() {
			return area;
		}

		@Override
		public void complete() {
			if(exits.stream().count() == 2) {
				exits = new PathExits(exits);
			}
			else {
				exits = ExitMap.of(exits);
			}
		}
	}

	private final Deque<Location> path = new ArrayDeque<>();
	private final Area area;
	private final Link link;

	private Direction dir = Direction.NORTH;

	/**
	 * Constructor.
	 * @param area		Area of this path
	 * @param route		Optional route-type of this path
	 */
	public Path(Area area, Route route) {
		this.area = notNull(area);
		this.link = route == null ? Link.DEFAULT : RouteLink.of(route);
	}

	/**
	 * Sets the direction of the next location(s) in this path.
	 * @param dir Direction
	 */
	public Path direction(Direction dir) {
		this.dir = dir;
		return this;
	}

	/**
	 * Adds an intermediate path location to this path.
	 * @param descriptor Location descriptor
	 */
	public Path add(Location.Descriptor descriptor) {
		final PathLocation loc = new PathLocation(descriptor, area);
		return add(loc);
	}

	/**
	 * Adds a junction to this path.
	 * @param junction Junction connector
	 */
	public Path add(Location junction) {
		// Link path locations
		if(!path.isEmpty()) {
			final Location prev = path.getLast();
			link(prev, dir, junction);
			link(junction, dir.reverse(), prev);
		}

		// Add location to path
		path.addLast(junction);

		return this;
	}

	/**
	 * Links path locations.
	 * @param start		Start location
	 * @param dir		Link direction
	 * @param end		End location
	 */
	private void link(Location start, Direction dir, Location end) {
		start.add(Exit.of(dir, link, end));
	}

	/**
	 * @return Connectors for the start/end locations
	 * @throws IllegalArgumentException if the path is empty or only contains a single location
	 */
	public List<Location> connectors() {
		if(path.size() < 2) throw new IllegalArgumentException("Incomplete path");
		return List.of(path.getFirst(), path.getLast());
	}
}
