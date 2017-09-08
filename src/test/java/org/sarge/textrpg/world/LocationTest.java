package org.sarge.textrpg.world;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectLink;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.LinkWrapper.ReversePolicy;
import org.sarge.textrpg.world.Location.Property;

public class LocationTest {
    private static final Set<Property> PROPS = new HashSet<>(Arrays.asList(Property.WATER, Property.LIGHT));

	private Location loc, dest;
	private LinkWrapper link;
	private Actor actor;

	@Before
	public void before() {
		dest = new Location("dest", Area.ROOT, Terrain.DESERT, PROPS, Collections.emptyList());
		loc = new Location("location", Area.ROOT, Terrain.DESERT, PROPS, Collections.singleton("decoration"));
		link = new LinkWrapper(Direction.DOWN, new RouteLink(Route.ROAD), dest, Direction.UP, ReversePolicy.INVERSE);
		actor = mock(Actor.class);
	}

	@Test
	public void constructor() {
		assertEquals("location", loc.getName());
		assertEquals(Area.ROOT, loc.getArea());
		assertEquals(Terrain.DESERT, loc.getTerrain());
		assertArrayEquals(new String[]{"decoration"}, loc.getDecorations().toArray());
		assertNotNull(loc.contents());
		assertNotNull(loc.getExits());
		assertEquals(0, loc.getExits().size());
		assertNotNull(Location.getSurfaces());
		assertEquals("location", loc.parentName());
	}

	@Test
	public void properties() {
        assertTrue(loc.isProperty(Property.WATER));
        assertTrue(loc.isProperty(Property.LIGHT));
        assertFalse(loc.isProperty(Property.FISH));
	}

	@Test
	public void add() {
		loc.add(link);
		assertNotNull(loc.getExits());
		assertEquals(1, loc.getExits().size());

		final Exit exit = loc.getExits().get(Direction.DOWN);
		assertNotNull(exit);
		assertEquals(dest, exit.getDestination());
		assertEquals(link.getLink(), exit.getLink());
		assertEquals(true, exit.perceivedBy(actor));
	}

	@Test
	public void reverseLink() {
		loc.add(link);
		assertEquals(1, dest.getExits().size());
		final Exit reverse = dest.getExits().get(Direction.UP);
		assertNotNull(reverse);
		assertEquals(loc, reverse.getDestination());
		assertEquals(Route.ROAD, reverse.getLink().route());
		assertEquals(Optional.empty(), reverse.getLink().controller());
	}

	@Test
	public void reverseLinkSimple() {
		link = new LinkWrapper(Direction.DOWN, Link.DEFAULT, dest, Direction.UP, ReversePolicy.SIMPLE);
		loc.add(link);
		final Exit reverse = dest.getExits().get(Direction.UP);
		assertNotNull(reverse);
		assertEquals(loc, reverse.getDestination());
		assertEquals(Route.NONE, reverse.getLink().route());
		assertEquals(Optional.empty(), reverse.getLink().controller());
	}

	@Test
	public void reverseLinkOneWay() {
		link = new LinkWrapper(Direction.DOWN, Link.DEFAULT, dest, Direction.UP, ReversePolicy.ONE_WAY);
		loc.add(link);
		assertEquals(null, dest.getExits().get(Direction.UP));
	}

	@Test
	public void closedLocation() {
		final Location closed = new Location(dest) {
			@Override
			public boolean isOpen() {
				return false;
			}
		};
		link = new LinkWrapper(Direction.EAST, Link.DEFAULT, closed);
		loc.add(link);
		assertEquals(true, loc.getExits().isEmpty());
	}

    @Test
    public void isLightAvailable() {
        assertEquals(true, loc.isLightAvailable(true));
        assertEquals(false, loc.isLightAvailable(false));
    }

	@Test
	public void isLightAvailableTerrainDark() {
		loc = new Location("location", Area.ROOT, Terrain.UNDERGROUND, Collections.emptySet(), Collections.emptyList());
        assertEquals(false, loc.isLightAvailable(true));
        assertEquals(false, loc.isLightAvailable(false));
	}

	@Test
    public void isLightAvailableTerrainDarkOverride() {
		loc = new Location("location", Area.ROOT, Terrain.UNDERGROUND, Collections.singleton(Property.LIGHT), Collections.emptyList());
        assertEquals(true, loc.isLightAvailable(true));
        assertEquals(true, loc.isLightAvailable(false));
	}

    @Test
    public void isLightAvailableOverride() {
        loc = new Location("location", Area.ROOT, Terrain.FARMLAND, Collections.emptySet(), Collections.emptyList());
        assertEquals(false, loc.isLightAvailable(true));
        assertEquals(false, loc.isLightAvailable(false));
    }

	@Test
	public void isArtificialLightAvailable() {
		final Thing light = mock(Thing.class);
		when(light.emission(Emission.Type.LIGHT)).thenReturn(Optional.of(new Emission(Emission.Type.LIGHT, Percentile.HALF)));
		loc.contents().add(light);
		assertEquals(true, loc.isArtificialLightAvailable());
	}

	@Test
	public void describe() throws ActionException {
		// Add some contents
		final WorldObject obj = new WorldObject(new ObjectDescriptor("object"));
		obj.setParent(loc);

		// Add a link that should be listed in the exits
		loc.add(link);
		when(actor.perceives(any(Hidden.class))).thenReturn(true);

		// Add a link with a visible controller object
		final WorldObject controller = new WorldObject(new ObjectDescriptor("portal")) {
			@Override
			public Optional<Openable> openableModel() {
				return Optional.of(new Openable());
			}
		};
		final Link other = new ObjectLink(Route.NONE, Script.NONE, Size.NONE, controller, "reason");
		loc.add(new LinkWrapper(Direction.EAST, other, loc));

		// Build description
		final Description description = loc.describe(true, actor);
		assertNotNull(description);
		assertEquals("location.description", description.getKey());
		assertNotNull(description.getDescriptions());
		assertEquals(3, description.getDescriptions().count());

		// Check object description
		final Iterator<Description> itr = description.getDescriptions().iterator();
		final Description object = itr.next();
		assertEquals("description.dropped", object.getKey());
		assertEquals("{object}", object.get("name"));

		// Check link controller description
		final Description controllerDescription = itr.next();
		assertEquals("description.dropped", controllerDescription.getKey());
		assertEquals("{portal}", controllerDescription.get("name"));

		// Check exits description
		final Description exits = itr.next();
		assertEquals("exit.exits", exits.getKey());
		assertEquals("={exit.down}=", exits.get("down"));
		assertEquals("{exit.east}", exits.get("east"));
	}

	@Test
	public void describeEmptyExits() {
		final Description description = loc.describe(true, actor);
		assertEquals("exit.exits.none", description.getDescriptions().iterator().next().getKey());
	}

	@Test
	public void describeDark() {
		final Description description = loc.describe(false, actor);
		assertEquals("location.description.dark", description.getKey());
	}

	@Test
	public void describeTerrainDark() {
		loc = new Location("dark", Area.ROOT, Terrain.UNDERGROUND, Collections.emptySet(), Collections.emptyList());
		final Description description = loc.describe(true, actor);
		assertEquals("location.description.dark", description.getKey());
	}

	@Test(expected = IllegalArgumentException.class)
	public void duplicateExit() {
		loc.add(link);
		loc.add(link);
	}

	@Test(expected = IllegalArgumentException.class)
	public void duplicateReverseExit() {
		final LinkWrapper other = new LinkWrapper(Direction.UP, Link.DEFAULT, loc, Direction.DOWN, ReversePolicy.SIMPLE);
		loc.add(link);
		dest.add(other);
	}
}
