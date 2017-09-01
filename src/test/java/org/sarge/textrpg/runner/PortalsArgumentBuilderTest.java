package org.sarge.textrpg.runner;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.LinkWrapper;
import org.sarge.textrpg.world.LinkWrapper.ReversePolicy;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

public class PortalsArgumentBuilderTest {
	private Location loc;
	private Actor actor;

	@Before
	public void before() {
		loc = new Location("loc", Area.ROOT, Terrain.DESERT, Collections.emptySet(), Collections.emptyList());
		actor = mock(Actor.class);
	}

	private void create(boolean perceives, Thing obj, Direction dir, Location loc) {
		final Link link = mock(Link.class);
		when(link.getController()).thenReturn(Optional.ofNullable(obj));
		when(actor.perceives(obj)).thenReturn(perceives);
		loc.add(new LinkWrapper(dir, link, loc, dir, ReversePolicy.ONE_WAY));
	}

	@Test
	public void portals() {
		// Create a link that should be invisible to the actor
		create(false, null, Direction.NORTH, loc);

		// Create a visible link without a controller
		create(true, null, Direction.EAST, loc);

		// Create a link with a controller
		final Thing obj = mock(Thing.class);
		create(true, obj, Direction.WEST, loc);

		// Check only the link with the controller is listed
		final ArgumentBuilder builder = new PortalsArgumentBuilder(loc);
		assertArrayEquals(new Object[]{obj}, builder.stream(actor).toArray());
	}
}
