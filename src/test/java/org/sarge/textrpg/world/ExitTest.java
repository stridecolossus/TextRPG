package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;

public class ExitTest {
	private Exit exit;
	private Link link;
	private Location dest;
	private Actor actor;

	@BeforeEach
	public void before() {
		// Create link
		link = mock(Link.class);
		when(link.route()).thenReturn(Route.NONE);

		// Create destination
		dest = mock(Location.class);
		when(dest.terrain()).thenReturn(Terrain.GRASSLAND);

		// Create exit
		exit = Exit.of(Direction.EAST, link, dest);
		actor = mock(Actor.class);
	}

	@Test
	public void constructor() {
		assertEquals(Direction.EAST, exit.direction());
		assertEquals(link, exit.link());
		assertEquals(dest, exit.destination());
	}

	@Test
	public void constructorDefaultLink() {
		final Exit def = Exit.of(Direction.EAST, dest);
		assertEquals(Direction.EAST, def.direction());
		assertEquals(Link.DEFAULT, def.link());
		assertEquals(dest, def.destination());
	}

	@Test
	public void isPerceived() {
		assertEquals(true, exit.isPerceivedBy(actor));
	}

	@Test
	public void isPerceivedUnknownController() {
		when(link.controller()).thenReturn(Optional.of(mock(Thing.class)));
		assertEquals(false, exit.isPerceivedBy(actor));
	}

	@Test
	public void describe() {
		exit = Exit.of(Direction.EAST, RouteLink.of(Route.LANE), dest);
		when(dest.name()).thenReturn("dest");
		final var expected = new Description.Builder("location.exit.default")
			.add("dest", "dest")
			.add("dir", "direction.east")
			.build();
		assertEquals(expected, exit.describe());
	}

	@Test
	public void equals() {
		assertEquals(exit, exit);
		assertEquals(exit, Exit.of(Direction.EAST, link, dest));
	}

	@Test
	public void notEquals() {
		assertNotEquals(exit, null);
		assertNotEquals(exit, Exit.of(Direction.WEST, dest));
	}
}
