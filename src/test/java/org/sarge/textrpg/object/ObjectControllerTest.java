package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;

public class ObjectControllerTest {
	private static final Duration DURATION = Duration.ofMillis(42);

	private ObjectController controller;
	private Event.Queue.Manager manager;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("object").reset(DURATION).decay(DURATION).build();
		obj = mock(WorldObject.class);
		when(obj.name()).thenReturn("object");
		when(obj.descriptor()).thenReturn(descriptor);
		manager = new Event.Queue.Manager();
		controller = new ObjectController(manager);
	}

	@Test
	public void decay() {
		controller.decay(obj);
		manager.advance(DURATION.toMillis());
		verify(obj).decay();
	}

	@Test
	public void reset() {
		final Event reset = mock(Event.class);
		controller.reset(obj, reset);
		manager.advance(DURATION.toMillis());
		verify(reset).execute();
	}

	@Test
	public void other() {
		// Create two locations
		final Location loc = mock(Location.class);
		final Location other = mock(Location.class);
		when(other.terrain()).thenReturn(Terrain.DESERT);

		// Create portal link
		final Link link = mock(Link.class);
		when(link.controller()).thenReturn(Optional.of(obj));
		when(link.route()).thenReturn(Route.NONE);

		// Create portal exit
		final Exit exit = Exit.of(Direction.EAST, link, other);
		when(loc.exits()).thenReturn(ExitMap.of(exit));

		// Check both sides
		assertEquals(other, controller.other(loc, obj));
		assertEquals(loc, controller.other(other, obj));
	}
}
