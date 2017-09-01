package org.sarge.textrpg.entity;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Clock;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.util.DataTableCalculator;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.LinkWrapper;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;

public class MovementControllerTest extends ActionTest {
	private MovementController controller;
	private Location dest;
	private Link link;

	@Before
	public void before() {
		// Create controller
		final Clock clock = mock(Clock.class);
		final DataTableCalculator move = mock(DataTableCalculator.class);
		final DataTableCalculator tracks = mock(DataTableCalculator.class);
		when(move.multiply(Terrain.DESERT, Route.NONE, actor.getStance())).thenReturn(3f);
		when(tracks.multiply(Terrain.DESERT, Route.NONE, actor.getStance())).thenReturn(0.5f);
		controller = new MovementController(clock, move, tracks, 1);

		// Create a link with a controller
		final Thing obj = mock(Thing.class);
		link = mock(Link.class);
		when(link.getController()).thenReturn(Optional.of(obj));
		when(link.getScript()).thenReturn(mock(Script.class));
		when(link.getSize()).thenReturn(Size.MEDIUM);
		when(actor.perceives(obj)).thenReturn(true);

		// Link to location
		dest = new Location("dest", Area.ROOT, Terrain.DESERT, Collections.emptySet(), Collections.emptyList());
		loc.add(new LinkWrapper(Direction.EAST, link, dest));

		// Ensure sufficient stamina
		@SuppressWarnings("unchecked")
		final IntegerMap<EntityValue> values = mock(IntegerMap.class);
		when(values.get(EntityValue.STAMINA)).thenReturn(3);
		when(actor.getValues()).thenReturn(values);

		// Give the actor a race (for tracks)
		final Race race = new Race.Builder("race").build();
		when(actor.getRace()).thenReturn(race);
		when(actor.getSize()).thenReturn(Size.SMALL);

		// Add an emission to the actor
		final Emission emission = Emission.light(Percentile.ONE);
		when(actor.getEmission(any(Emission.Type.class))).thenReturn(Optional.empty());
		when(actor.getEmission(Emission.Type.LIGHT)).thenReturn(Optional.of(emission));
	}

	@Test
	public void move() throws ActionException {
		// Move
		final Description next = controller.move(actor, Direction.EAST, 2, true);
		verify(actor).setParent(dest);
		assertNotNull(next);

		// Check stamina consumed
		verify(actor).modify(EntityValue.STAMINA, -1);

		// Check script
		verify(link.getScript()).execute(actor);

		// Check movement notifications
		// TODO
		//verify(loc).broadcast(actor, new MovementNotification(actor, false, Direction.EAST));
		//verify(dest).broadcast(actor, new MovementNotification(actor, true, Direction.WEST));

		// Check emission notification
		// TODO
		// verify(start).notify(new MovementNotification(actor, false, Direction.EAST));

		// Check tracks
		// TODO
		/*
		assertEquals(1, loc.getContents().stream().count());
		final Tracks t = (Tracks) loc.getContents().stream().iterator().next();
		assertEquals("race", t.getName());
		assertEquals(Direction.EAST, t.getDirection());
		assertEquals(loc, t.getParent());
		assertEquals(new Percentile(0.5f), t.getVisibility());
		assertTrue(t.getCreationTime() == 0);
		*/
	}

	@Test
	public void moveNotTraversable() throws ActionException {
		when(link.reason(actor)).thenReturn("move.cannot.traverse");
		expect("move.cannot.traverse");
		controller.move(actor, Direction.EAST, 1, true);
	}

	@Test
	public void moveSizeConstraint() throws ActionException {
		when(link.reason(actor)).thenReturn("move.link.constraint");
		when(actor.getSize()).thenReturn(Size.LARGE);
		expect("move.link.constraint");
		controller.move(actor, Direction.EAST, 1, true);
	}

	@Test
	public void moveInsufficientStamina() throws ActionException {
		when(actor.getValues().get(EntityValue.STAMINA)).thenReturn(0);
		expect("move.insufficient.stamina");
		controller.move(actor, Direction.EAST, 1, true);
	}
}
