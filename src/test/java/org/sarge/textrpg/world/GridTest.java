package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sarge.textrpg.util.Matrix;
import org.sarge.textrpg.util.Matrix.Coordinates;
import org.sarge.textrpg.world.Grid.Builder.Cursor;
import org.sarge.textrpg.world.Grid.Neighbour;

public class GridTest {
	private Grid.Builder builder;
	private Location.Descriptor descriptor;
	private Cursor cursor;

	@BeforeEach
	public void before() {
		// Create grid with empty bottom-right location
		final Matrix<Location.Descriptor> grid = new Matrix<>(3, 4);
		descriptor = new Location.Descriptor.Builder().name("loc").terrain(Terrain.HILL).build();
		grid.fill(descriptor);
		grid.set(2, 3, null);

		// Create builder
		builder = new Grid.Builder(Area.ROOT, grid);
		cursor = builder.cursor(1, 2);
	}

	@Test
	public void build() {
		final Grid grid = builder.build();
		assertNotNull(grid);
	}

	@Test
	public void cursorOutOfBounds() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> builder.cursor(-1, 2));
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> builder.cursor(3, 4));
	}

	@Test
	public void cursorEmptyLocation() {
		assertThrows(IllegalArgumentException.class, () -> builder.cursor(2, 3));
	}

	@Nested
	class BlockedExits {
		@Test
		public void block() {
			cursor.block(Direction.EAST, true);
			assertEquals(3, cursor.connector().exits().stream().count());
			assertEquals(1, builder.cursor(2, 2).connector().exits().stream().count());
		}

		@Test
		public void blockEmptyDestination() {
			cursor = builder.cursor(1, 3);
			assertThrows(IllegalArgumentException.class, () -> cursor.block(Direction.EAST, false));
		}

		@Test
		public void blockEdge() {
			cursor = builder.cursor(0, 0);
			assertThrows(ArrayIndexOutOfBoundsException.class, () -> cursor.block(Direction.NORTH, false));
			assertThrows(ArrayIndexOutOfBoundsException.class, () -> cursor.block(Direction.WEST, false));
		}

		@Test
		public void blockInvalidDirection() {
			assertThrows(IllegalArgumentException.class, () -> cursor.block(Direction.UP, false));
			assertThrows(IllegalArgumentException.class, () -> cursor.block(Direction.DOWN, false));
		}

		@Test
		public void blockAlreadyBlocked() {
			cursor.block(Direction.EAST, false);
			assertThrows(IllegalArgumentException.class, () -> cursor.block(Direction.EAST, false));
		}
	}

	@Nested
	class Routes {
		@Test
		public void route() {
			cursor.route(Route.LANE, List.of(Direction.WEST, Direction.NORTH, Direction.EAST));
		}

		@Test
		public void routeEmptyPath() {
			assertThrows(IllegalArgumentException.class, () -> cursor.route(Route.LANE, List.of()));
		}

		@Test
		public void routeInvalidRoute() {
			assertThrows(IllegalArgumentException.class, () -> cursor.route(Route.NONE, List.of(Direction.EAST)));
		}

		@Test
		public void routeEmptyLocation() {
			assertThrows(IllegalArgumentException.class, () -> cursor.route(Route.LANE, List.of(Direction.EAST, Direction.SOUTH)));
		}

		@Test
		public void routeOutOfBounds() {
			assertThrows(ArrayIndexOutOfBoundsException.class, () -> cursor.route(Route.LANE, List.of(Direction.WEST, Direction.WEST)));
		}

		@Test
		public void routeBlockedExit() {
			cursor.block(Direction.EAST, false);
			assertThrows(IllegalArgumentException.class, () -> cursor.route(Route.LANE, List.of(Direction.EAST)));
		}
	}

	@Nested
	class Neighbours {
		@ParameterizedTest
		@CsvSource({
			// Zero offset
			"0, NORTH, 0/0, 0/3",
			"0, NORTH, 2/0, 2/3",
			"0, SOUTH, 0/3, 0/0",
			"0, SOUTH, 2/3, 2/0",
			"0, EAST,  0/0, 0/0",
			"0, EAST,  0/3, 0/3",
			"0, WEST,  0/0, 2/0",
			"0, WEST,  0/3, 2/3",
			// 2 right/down
			"2, NORTH, 2/0, 0/3",
			"2, SOUTH, 2/3, 0/0",
			"2, EAST,  2/2, 0/0",
			"2, EAST,  2/3, 0/1",
			"2, WEST,  0/2, 2/0",
			"2, WEST,  0/3, 2/1",
			// 2 left/up
			"-2, NORTH, 0/0, 2/3",
			"-2, SOUTH, 0/3, 2/0",
			"-2, EAST,  2/0, 0/2",
			"-2, WEST,  0/0, 2/2",
		})
		public void find(int offset, Direction side, Coordinates coords, Coordinates expected) {
			final Neighbour neighbour = new Neighbour(builder.build(), offset);
			assertEquals(expected, neighbour.coordinates(side, coords));
		}
	}

	@Nested
	class AttachedNeighbours {
		private Grid neighbour;

		private Grid create() {
			final Matrix<Location.Descriptor> grid = new Matrix<>(3, 4);
			grid.fill(descriptor);
			return new Grid.Builder(Area.ROOT, grid).build();
		}

		@BeforeEach
		public void before() {
			neighbour = create();
		}

		@Test
		public void attach() {
			// Attach neighbour to right-hand side, 2 elements down
			builder.neighbour(neighbour, Direction.EAST, 2);

			// Lookup neighbouring locations
			final Grid grid = builder.build();
			final Location left = grid.get(new Coordinates(2, 2));
			final Location right = neighbour.get(new Coordinates(0, 0));

			// Check locations on edge can link to neighbouring grid
			assertEquals(Optional.empty(), grid.get(new Coordinates(2, 0)).exits().find(Direction.EAST));
			assertEquals(Optional.empty(), grid.get(new Coordinates(2, 1)).exits().find(Direction.EAST));
			assertEquals(Optional.of(new Exit(Direction.EAST, Link.DEFAULT, right)), grid.get(new Coordinates(2, 2)).exits().find(Direction.EAST));

			// Check reverse exit
			assertEquals(Optional.of(new Exit(Direction.WEST, Link.DEFAULT, left)), right.exits().find(Direction.WEST));
		}

		@Test
		public void attachAlreadyAttached() {
			builder.neighbour(neighbour, Direction.EAST, 0);
			assertThrows(IllegalArgumentException.class, () -> builder.neighbour(neighbour, Direction.WEST, 0));
		}

		@Test
		public void attachDuplicateSide() {
			builder.neighbour(neighbour, Direction.EAST, 0);
			assertThrows(IllegalArgumentException.class, () -> builder.neighbour(create(), Direction.EAST, 0));
		}

		@Test
		public void attachInvalidOffset() {
			assertThrows(IllegalArgumentException.class, () -> builder.neighbour(neighbour, Direction.NORTH, -3));
			assertThrows(IllegalArgumentException.class, () -> builder.neighbour(neighbour, Direction.SOUTH, +3));
			assertThrows(IllegalArgumentException.class, () -> builder.neighbour(neighbour, Direction.EAST, +4));
			assertThrows(IllegalArgumentException.class, () -> builder.neighbour(neighbour, Direction.WEST, -4));
		}
	}
}
