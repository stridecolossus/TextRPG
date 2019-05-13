package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.MovementMode;
import org.sarge.textrpg.object.Rope;
import org.sarge.textrpg.util.Clock;
import org.sarge.textrpg.util.Percentile;

public class TracksControllerTest {
	private TracksController controller;
	private Clock clock;

	@BeforeEach
	public void before() {
		clock = mock(Clock.class);
		controller = new TracksController(clock);
	}

	@Test
	public void add() {
		// Create weather
		final Weather weather = new Weather.Builder()
			.min(Weather.Component.PRECIPITATION, 2)
			.min(Weather.Component.TEMPERATURE, 2)
			.build();

		// Generate weather history
		weather.randomise(2);

		// Create actor
		final Entity actor = mock(Entity.class);
		final MovementMode mode = mock(MovementMode.class);
		when(mode.tracks()).thenReturn(Percentile.HALF);
		when(actor.name()).thenReturn("actor");
		when(actor.movement()).thenReturn(mode);
		when(mode.mover()).thenReturn(actor);

		// Create trail
		final Trail trail = mock(Trail.class);
		when(mode.trail()).thenReturn(trail);

		// Create exit
		final Area area = new Area.Builder("area").weather(mock(Weather.class)).build();
		final Location loc = new DefaultLocation(new Location.Descriptor("loc"), area);
		final Exit exit = Exit.of(Direction.EAST, RouteLink.of(Route.LANE), loc);
		when(actor.location()).thenReturn(loc);

		// Add tracks
		controller.add(actor, exit, loc);
		assertEquals(1, loc.tracks().count());

		// Check tracks added to trail
		final Tracks tracks = loc.tracks().iterator().next();
		verify(trail).add(tracks);
		assertEquals("actor", tracks.creator());
		assertEquals(Direction.EAST, tracks.direction());
		assertEquals(Percentile.HALF, tracks.visibility());
		assertEquals(0L, tracks.created());
	}

	@Test
	public void isTracksLocation() {
		isTracksLocation(Terrain.FARMLAND, Link.DEFAULT, true);
	}

	@Test
	public void isTracksLocationUnsuitableTerrain() {
		isTracksLocation(Terrain.INDOORS, Link.DEFAULT, false);
	}

	@Test
	public void isTracksLocationRopeLink() {
		isTracksLocation(Terrain.FARMLAND, mock(Rope.RopeLink.class), false);
	}

	private static void isTracksLocation(Terrain terrain, Link link, boolean expected) {
		final Location loc = new DefaultLocation(new Location.Descriptor.Builder().name("loc").terrain(terrain).build(), Area.ROOT);
		final Exit exit = Exit.of(Direction.EAST, link, loc);
		assertEquals(expected, TracksController.isTracksLocation(loc, exit));
	}
}
