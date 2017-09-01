package org.sarge.textrpg.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.world.Location.Property;
import org.sarge.textrpg.world.MoveableLocation.Stage;

public class MoveableLocationTest {
	private MoveableLocation loc;
	private Stage one, two;

	@Before
	public void before() {
		one = new Stage(new Location("1", Area.ROOT, Terrain.FARMLAND, Collections.emptySet(), Collections.emptyList()), 1);
		two = new Stage(new Location("2", Area.ROOT, Terrain.DESERT, Collections.singleton(Property.WATER), Collections.emptyList()), 2);
		loc = new MoveableLocation("loc", Arrays.asList(one, two), true);
	}

	@After
	public void after() {
		MoveableLocation.QUEUE.reset();
	}

	@Test
	public void constructor() {
		assertEquals("loc", loc.getName());
		assertEquals(one, loc.getLocation());
		assertEquals(1, MoveableLocation.QUEUE.stream().count());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void addLinkStage() {
		one.add(new LinkWrapper(Direction.EAST, Link.DEFAULT, loc));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void addLink() {
		loc.add(new LinkWrapper(Direction.EAST, Link.DEFAULT, loc));
	}

	@Test
	public void describe() {
		final Description description = loc.describe(true, mock(Actor.class));
		assertEquals("{1}", description.get("name"));
		assertEquals("{loc}", description.get("area"));
	}

	@Test
	public void stages() {
		// Check start stage
		assertEquals(one, loc.getLocation());
		assertEquals(Terrain.FARMLAND, loc.getTerrain());
		assertEquals(Area.ROOT, loc.getArea());
		assertEquals(false, loc.isProperty(Property.WATER));

		// Transition to second stage
		loc.move();
		assertEquals(two, loc.getLocation());
		assertEquals(Terrain.DESERT, loc.getTerrain());
		assertEquals(Area.ROOT, loc.getArea());
        assertEquals(true, loc.isProperty(Property.WATER));

		// Transition back to start
		loc.move();
		assertEquals(one, loc.getLocation());
	}

	@Test
	public void exits() {
		// Add some links
		final Location dest = new Location("dest", Area.ROOT, Terrain.DESERT, Collections.emptySet(), Collections.emptyList());
		dest.add(new LinkWrapper(Direction.EAST, Link.DEFAULT, one));
		dest.add(new LinkWrapper(Direction.SOUTH, Link.DEFAULT, two));

		// Check links at start
		assertEquals(1, loc.getExits().size());
		assertNotNull(loc.getExits().get(Direction.WEST));
		assertEquals(dest, loc.getExits().get(Direction.WEST).getDestination());
		assertNotNull(dest.getExits().get(Direction.EAST));
		assertEquals(null, dest.getExits().get(Direction.SOUTH));

		// Check links after transition
		loc.move();
		assertEquals(1, loc.getExits().size());
		assertEquals(null, loc.getExits().get(Direction.SOUTH));
		assertEquals(null, dest.getExits().get(Direction.EAST));
		assertNotNull(dest.getExits().get(Direction.SOUTH));
	}
}
