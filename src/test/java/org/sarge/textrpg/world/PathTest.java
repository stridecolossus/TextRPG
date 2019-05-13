package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PathTest {
	private Path path;
	private Location.Descriptor descriptor;

	@BeforeEach
	public void before() {
		descriptor = new Location.Descriptor.Builder().name("loc").build();
		path = new Path(Area.ROOT, Route.TRAIL);
	}

	@Test
	public void empty() {
		assertThrows(IllegalArgumentException.class, () -> path.connectors());
	}

	@Test
	public void incomplete() {
		path.add(descriptor);
		assertThrows(IllegalArgumentException.class, () -> path.connectors());
	}

	@Test
	public void add() {
		// Add three path locations
		path.direction(Direction.EAST);
		path.add(descriptor);
		path.add(descriptor);

		// Check start/end connectors
		final var connectors = path.connectors();
		assertNotNull(connectors);
		assertEquals(2, connectors.size());

		// Extract start/end locations
		final Location start = connectors.get(0);
		final Location end = connectors.get(1);

		// Check start exits
		final Link link = RouteLink.of(Route.TRAIL);
		assertEquals(1, start.exits().stream().count());
		assertEquals(Optional.of(Exit.of(Direction.EAST, link, end)), start.exits().find(Direction.EAST));

		// Check end exits
		assertEquals(1, end.exits().stream().count());
		assertEquals(Optional.of(Exit.of(Direction.WEST, link, start)), end.exits().find(Direction.WEST));
	}

	@Test
	public void junction() {
		// Create a junction
		final DefaultLocation junction = mock(DefaultLocation.class);
		when(junction.exits()).thenReturn(mock(ExitMap.class));

		// Add junction and another location
		path.add(junction);
		path.add(descriptor);

		// Check start/end connectors
		final var connectors = path.connectors();
		assertEquals(2, connectors.size());
		assertTrue(connectors.contains(junction));

		// Check path
		final Location start = connectors.get(0);
		final Location end = connectors.get(1);
		assertEquals(junction, start);
		verify(junction).add(Exit.of(Direction.NORTH, RouteLink.of(Route.TRAIL), end));
	}
}
