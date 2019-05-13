package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.collection.SoftMap;
import org.sarge.lib.collection.StrictMap;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.util.Matrix;
import org.sarge.textrpg.util.Matrix.Coordinates;
import org.sarge.textrpg.world.ExitMap.MutableExitMap;
import org.sarge.textrpg.world.Grid.Builder.Cursor;

/**
 * A <i>grid</i> is a two-dimensional array of locations used to model large open spaces in the world.
 * <p>
 * Each location in a grid has <i>default</i> exits in the cardinal directions except at the edges of the grid.
 * To add variety and disguise the 2D structure a grid can contain empty (or <tt>null</tt>) locations and <i>blocked</i> or custom exits.
 * <p>
 * In general grids are intended for large wilderness areas where most locations have exits in every direction and the number of points-of-interest is relatively low.
 * Grid locations are relatively lightweight in terms of memory usage at the expense of the runtime overhead of on-demand generation of the exits for each visited location.
 * <p>
 * The {@link Builder#cursor(int, int)} method is used to get a <i>cursor</i> to a location in the grid which can be used to modify the grid:
 * <ul>
 * <li>{@link Cursor#block(Direction)} blocks a location exit in the given direction</li>
 * <li>{@link Cursor#connector()} is used to expose a connector to the grid</li>
 * <li>{@link Cursor#route(Route, List)} can be used to define a route within the grid</li>
 * </ul>
 * <p>
 * Grids can be attached using the {@link Builder#neighbour(Grid, Direction, int)} method.
 * <p>
 * Notes:
 * <ul>
 * <li>Default exits are simple links to neighbours, i.e. {@link Link#DEFAULT}</li>
 * <li>All locations in a grid share a common {@link Area}</li>
 * </ul>
 * @author Sarge
 */
public class Grid {
	/**
	 * Cardinal grid directions.
	 */
	private static final List<Direction> CARDINAL = Arrays.stream(Direction.values()).filter(Direction::isCardinal).collect(toList());

	/**
	 * Empty custom exits.
	 */
	private static final Map<Direction, Exit> EMPTY = Map.of();

	/**
	 * Grid location instance.
	 */
	private class GridLocation extends Location {
		private final Coordinates coords;
		private final Contents contents = new Contents();

		/**
		 * Constructor.
		 * @param descriptor		Location descriptor
		 * @param coords			Coordinates
		 */
		private GridLocation(Location.Descriptor descriptor, Coordinates coords) {
			super(descriptor, null);
			this.coords = notNull(coords);
		}

		@Override
		public Area area() {
			return area;
		}

		@Override
		public Contents contents() {
			return contents;
		}

		@Override
		public ExitMap exits() {
			if(exits == null) {
				exits = build(coords);
			}
			return exits;
		}
	}

	/**
	 * Neighbouring grid descriptor.
	 */
	static class Neighbour {
		private final Grid grid;
		private final int offset;

		/**
		 * Constructor.
		 * @param neighbour		Neighbouring grid
		 * @param offset		Offset
		 */
		Neighbour(Grid neighbour, int offset) {
			this.grid = notNull(neighbour);
			this.offset = offset;
		}

		/**
		 * Finds the destination of a neighbouring grid for the given coordinates.
		 * @param side			Neighbour side
		 * @param coords		Coordinates
		 * @return Destination or <tt>null</tt> if none
		 */
		private Location find(Direction side, Coordinates coords) {
			// Calculate destination coordinates
			final Coordinates dest = coordinates(side, coords);

			// Ignore if out-of-bounds
			if(grid.matrix.isOutOfBounds(dest)) {
				return null;
			}

			// Lookup destination
			return grid.get(dest);
		}

		/**
		 * Determines the coordinates of a neighbouring destination.
		 * @param side			Neighbour side
		 * @param coords		Coordinates
		 * @return Destination coordinates
		 */
		Coordinates coordinates(Direction side, Coordinates coords) {
			final int offset = (isHorizontal(side) ? coords.x : coords.y) - this.offset;
			switch(side) {
			case NORTH:		return new Coordinates(offset, grid.matrix.height() - 1);
			case SOUTH:		return new Coordinates(offset, 0);
			case EAST:		return new Coordinates(0, offset);
			case WEST:		return new Coordinates(grid.matrix.width() - 1, offset);
			default:		throw new RuntimeException();
			}
		}
	}

	private final Area area;
	private final Matrix<Location.Descriptor> matrix;
	private final Map<Coordinates, GridLocation> cache = new SoftMap<>();
	private final Map<Coordinates, Map<Direction, Exit>> custom = new StrictMap<>();
	private final Map<Direction, Neighbour> neighbours = new StrictMap<>();

	/**
	 * Constructor.
	 * @param area			Area
	 * @param matrix		Location descriptor matrix
	 */
	private Grid(Area area, Matrix<Location.Descriptor> matrix) {
		this.area = notNull(area);
		this.matrix = notNull(matrix);
	}

	/**
	 * Looks up a grid location.
	 * @param coords Grid coordinates
	 * @return Grid location or <tt>null</tt> if empty
	 * @throws ArrayIndexOutOfBoundsException if the given coordinates are out-of-bounds
	 */
	public Location get(Coordinates coords) {
		return cache.computeIfAbsent(coords, this::create);
	}

	/**
	 * Creates a grid location.
	 * @param coords Grid coordinates
	 * @return Grid location or <tt>null</tt> if empty
	 * @throws ArrayIndexOutOfBoundsException if the given coordinates are out-of-bounds
	 */
	private GridLocation create(Coordinates coords) {
		final Location.Descriptor descriptor = matrix.get(coords);
		if(descriptor == null) {
			return null;
		}
		else {
			return new GridLocation(descriptor, coords);
		}
	}

	/**
	 * Adds a custom exit in the given direction.
	 * @param coords		Exit coordinates
	 * @param dir			Direction
	 * @param exit			Exit descriptor or <tt>null</tt> for a blocked exit
	 * @throws IllegalArgumentException if the location at the given coordinates is empty or for a duplicate exit direction
	 */
	private void add(Coordinates coords, Direction dir, Exit exit) {
		final var exits = custom.computeIfAbsent(coords, key -> new HashMap<>());
		if(exits.containsKey(dir)) throw new IllegalArgumentException(String.format("Duplicate custom exit: coords=%s dir=%s", coords, dir));
		exits.put(dir, exit);
	}

	/**
	 * Moves the given coordinates in the given cardinal direction.
	 * @param coords	Coordinates
	 * @param dir 		Direction to move
	 * @return Moved coordinates
	 * @throws IllegalArgumentException if the given direction is not cardinal
	 */
	private static Coordinates move(Coordinates coords, Direction dir) {
		switch(dir) {
		case NORTH:		return new Coordinates(coords.x, coords.y - 1);
		case SOUTH:		return new Coordinates(coords.x, coords.y + 1);
		case EAST:		return new Coordinates(coords.x + 1, coords.y);
		case WEST:		return new Coordinates(coords.x - 1, coords.y);
		default:		throw new IllegalArgumentException("Invalid movement direction: " + dir);
		}
	}

	/**
	 * @param dir Direction
	 * @return Whether the given direction is <i>horizontal</i>
	 */
	private static boolean isHorizontal(Direction dir) {
		return (dir == Direction.NORTH) || (dir == Direction.SOUTH);
	}

	/**
	 * Determines the maximum dimension of the given side of this grid.
	 * @param side Side
	 * @return Maximum dimension
	 */
	private int max(Direction side) {
		if(isHorizontal(side)) {
			return matrix.width();
		}
		else {
			return matrix.height();
		}
	}

	/**
	 * Generates exits from the given coordinates in this grid.
	 * @param coords Coordinates
	 * @return Exits
	 */
	private ExitMap build(Coordinates coords) {
		// Lookup custom exits at this grid location
		final var map = custom.getOrDefault(coords, EMPTY);

		// Enumerate exits
		final MutableExitMap exits = new MutableExitMap();
		for(Direction dir : CARDINAL) {
			// Determine exit
			final Coordinates dest = move(coords, dir);
			if(map.containsKey(dir)) {
				final Exit exit = map.get(dir);
				if(exit == null) {
					// Skip blocked exits
					continue;
				}
				else {
					// Override with custom exit
					exits.add(exit);
				}
			}
			else {
				// Handle out-of-bounds locations
				if(matrix.isOutOfBounds(dest)) {
					// Lookup neighbouring grid on this side
					final Neighbour neighbour = neighbours.get(dir);
					if(neighbour == null) {
						// Out-of-bounds and no neighbouring grid
						continue;
					}

					// Find neighbouring destination
					final Location loc = neighbour.find(dir, coords);
					if(loc == null) {
						// No neighbouring destination
					}
					else {
						// Add exit to neighbour
						final Exit exit = Exit.of(dir, loc);
						exits.add(exit);
					}
					continue;
				}

				// Lookup location if present
				final Location loc = get(dest);
				if(loc == null) {
					continue;
				}

				// Add exit
				final Exit exit = Exit.of(dir, loc);
				exits.add(exit);
			}
		}

		// Optimise exits
		return ExitMap.of(exits);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("area", area.name())
			.append("w", matrix.width())
			.append("h", matrix.height())
			.build();
	}

	/**
	 * Builder for a grid.
	 */
	public static class Builder {
		private Grid grid;

		/**
		 * Constructor.
		 * @param area				Area
		 * @param descriptors		Location descriptors
		 */
		public Builder(Area area, Matrix<Location.Descriptor> descriptors) {
			grid = new Grid(area, descriptors);
		}

		/**
		 * Creates a cursor to this grid at the given coordinates.
		 * @param coords Grid coordinates
		 * @return Cursor
		 * @throws ArrayIndexOutOfBoundsException if the given coordinates are out-of-bounds
		 * @throws IllegalArgumentException if the location is empty
		 */
		public Cursor cursor(int x, int y) {
			final Coordinates coords = new Coordinates(x, y);
			if(grid.matrix.get(coords) == null) throw new IllegalArgumentException("Empty location: " + coords);
			return new Cursor(coords);
		}

		/**
		 * Attaches a neighbouring grid.
		 * <p>
		 * The <i>direction</i> specifies the <i>side</i> to attach, e.g. {@link Direction#EAST} attaches to the right-hand side of the grid.
		 * <p>
		 * The <i>offset</i> is the number of grid elements that the neighbour is offset relative to this grid.
		 * For top and bottom neighbours the offset axis is positive to the right, for side neighbours the axis is positive in the <i>down</i> direction.
		 * The offset cannot be larger than the dimensions of the neighbouring grid.
		 * <p>
		 * Note that grid attachments are bi-directional.
		 * <p>
		 * @param neighbour		Neighbour
		 * @param side			Side to attach
		 * @param offset		Offset
		 * @throws IllegalArgumentException if the direction is not {@link Direction#cardinal()} or already has an attachment
		 * @throws IllegalArgumentException if the offset is larger than the dimensions of the neighbouring grid
		 */
		public Builder neighbour(Grid neighbour, Direction side, int offset) {
			// Validate side
			if(!side.isCardinal()) throw new IllegalArgumentException("Invalid attachment side: " + side);
			if(grid.neighbours.values().stream().map(e -> e.grid).anyMatch(neighbour::equals)) throw new IllegalArgumentException("Duplicate neighbour: " + neighbour);

			// Validate offset
			if((offset <= -neighbour.max(side)) || (offset >= grid.max(side))) {
				throw new IllegalArgumentException(String.format("Offset cannot be large than the dimensions of this grid: grid=%s offset=%d", grid, offset));
			}

			// Attach neighbour
			grid.neighbours.put(side, new Neighbour(neighbour, offset));
			neighbour.neighbours.put(side.reverse(), new Neighbour(grid, -offset));
			return this;
		}

		/**
		 * Constructs this grid.
		 * @return Grid
		 * @throws IllegalArgumentException if this grid has already been constructed
		 */
		public Grid build() {
			if(grid == null) throw new IllegalStateException("Already built");
			final Grid result = grid;
			grid = null;
			return result;
		}

		/**
		 * Cursor to this grid.
		 */
		public final class Cursor extends Coordinates {
			/**
			 * Constructor.
			 * @param coords Grid coordinates
			 */
			private Cursor(Coordinates coords) {
				super(coords);
			}

			/**
			 * Adds a route starting at this location.
			 * @param route		Route type
			 * @param path		Route path
			 * @throws IllegalArgumentException if the route-type is not valid, the path is out-of-bounds, or any location already has an exit
			 */
			// TODO - bi-directional flag
			public void route(Route route, List<Direction> path) {
				// Validate route
				if(path.isEmpty()) throw new IllegalArgumentException("Empty route path");
				switch(route) {
				case NONE:
				case LADDER:
					throw new IllegalArgumentException("Invalid route type: " + route);
				}

				// Build route
				final RouteLink link = RouteLink.of(route);
				Coordinates prev = this;
				for(Direction dir : path) {
					final GridLocation next = move(prev, dir);
					grid.add(prev, dir, Exit.of(dir, link, next));
					prev = next.coords;
				}
			}

			/**
			 * Blocks the exit in the given direction from the location at this cursor.
			 * @param dir 		Direction
			 * @param bi		Whether the block is bi-directional
			 * @throws ArrayIndexOutOfBoundsException if the exit is out-of-bounds
			 * @throws IllegalArgumentException if the exit is already blocked or the destination is empty
			 */
			public void block(Direction dir, boolean bi) {
				final GridLocation dest = move(this, dir);
				grid.add(this, dir, null);
				if(bi) {
					grid.add(dest.coords, dir.reverse(), null);
				}
			}

			/**
			 * Moves in the given direction.
			 * @param coords		Coordinates
			 * @param dir			Direction
			 * @return Destination
			 * @throws ArrayIndexOutOfBoundsException if the move is out-of-bounds
			 * @throws IllegalArgumentException if the destination location is empty
			 */
			private GridLocation move(Coordinates coords, Direction dir) {
				final Coordinates dest = Grid.move(coords, dir);
				final Location loc = grid.get(dest);
				if(loc == null) throw new IllegalArgumentException(String.format("Empty location: coords=%s dir=%s", coords, dir));
				return (GridLocation) loc;
			}

			/**
			 * Exposes a connector at the given coordinates.
			 * @return Connector
			 * @throws IllegalArgumentException if the location is empty
			 */
			public Location connector() {
				final Location loc = grid.get(this);
				if(loc == null) throw new IllegalArgumentException("Cannot expose an empty location: " + this);
				return loc;
			}
		}
	}
}
