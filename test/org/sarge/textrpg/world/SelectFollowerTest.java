package org.sarge.textrpg.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.LinkWrapper.ReversePolicy;
import org.sarge.textrpg.world.SelectFollower.Policy;

public class SelectFollowerTest {
	private SelectFollower follower;
	private Entity actor;
	private Location loc, dest;
	private Link link;
	
	@Before
	public void before() {
		// Create follower
		follower = new SelectFollower(exit -> true, Policy.ONE);
		loc = ActionTest.createLocation();
		actor = mock(Entity.class);
		when(actor.getLocation()).thenReturn(loc);

		// Create destination
		dest = mock(Location.class);
		when(dest.isOpen()).thenReturn(true);
		when(dest.getArea()).thenReturn(Area.ROOT);

		// Create link with a controller
		final Thing obj = mock(Thing.class);
		link = mock(Link.class);
		when(link.isTraversable(actor)).thenReturn(true);
		when(link.getController()).thenReturn(Optional.of(obj));
		when(actor.perceives(obj)).thenReturn(true);
		
		// Add link
		final LinkWrapper wrapper = new LinkWrapper(Direction.EAST, link, dest, Direction.WEST, ReversePolicy.ONE_WAY);
		loc.add(wrapper);
	}
	
	@Test
	public void next() {
		assertEquals(Direction.EAST, follower.next(actor));
	}
	
	@Test
	public void nextEmpty() {
		final Location empty = ActionTest.createLocation();
		when(actor.getLocation()).thenReturn(empty);
		assertEquals(null, follower.next(actor));
	}

	@Test
	public void nextClosed() {
		when(link.isTraversable(actor)).thenReturn(false);
		assertEquals(null, follower.next(actor));
	}

	@Test
	public void nextHidden() {
		when(actor.perceives(link.getController().get())).thenReturn(false);
		assertEquals(null, follower.next(actor));
	}

	@Test
	public void nextRetrace() {
		final Location self = ActionTest.createLocation();
		final LinkWrapper wrapper = new LinkWrapper(Direction.EAST, link, self, Direction.WEST, ReversePolicy.ONE_WAY);
		self.add(wrapper);
		when(actor.getLocation()).thenReturn(self);
		follower.next(actor);
		assertEquals(Direction.EAST, follower.next(actor));
		follower.setAllowRetrace(false);
		assertEquals(null, follower.next(actor));
	}

	@Test
	public void nextBound() {
		when(dest.getArea()).thenReturn(null);
		assertEquals(null, follower.next(actor));
		follower.setBound(false);
		assertEquals(Direction.EAST, follower.next(actor));
	}

	@Test
	public void nextPolicyOne() {
		final LinkWrapper wrapper = new LinkWrapper(Direction.NORTH, link, dest, Direction.WEST, ReversePolicy.ONE_WAY);
		loc.add(wrapper);
		assertEquals(null, follower.next(actor));
	}

	@Test
	public void nextPolicyRandom() {
		final LinkWrapper wrapper = new LinkWrapper(Direction.NORTH, link, dest, Direction.WEST, ReversePolicy.ONE_WAY);
		loc.add(wrapper);
		follower = new SelectFollower(exit -> true, Policy.RANDOM);
		assertNotNull(follower.next(actor));
	}
	
	@Test
	public void terrainFilter() {
		final Predicate<Exit> filter = SelectFollower.terrain(Collections.singleton(Terrain.DESERT));
		when(dest.getTerrain()).thenReturn(Terrain.DESERT);
		assertEquals(true, filter.test(loc.getExits().get(Direction.EAST)));
	}
	
	@Test
	public void routeFilter() {
		final Predicate<Exit> filter = SelectFollower.route(Collections.singleton(Route.BRIDGE));
		when(link.getRoute()).thenReturn(Route.BRIDGE);
		follower = new SelectFollower(filter, Policy.ONE);
		assertEquals(Direction.EAST, follower.next(actor));
	}
}
