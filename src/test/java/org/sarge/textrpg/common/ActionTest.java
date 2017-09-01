package org.sarge.textrpg.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;

/**
 * Convenience base-class for tests that expect {@link ActionException}.
 * @author Sarge
 */
public abstract class ActionTest {
	/**
	 * Expected exception.
	 */
	@Rule
	public ExpectedException expected = ExpectedException.none();

	protected Entity actor;
	protected Location loc;

	@Before
	public void beforeActionTest() {
		// Create a location
		//final Contents contents = new Contents();
		//loc = mock(Location.class);
		//when(loc.getContents()).thenReturn(contents);
		//when(loc.getTerrain()).thenReturn(Terrain.GRASSLAND);
		loc = createLocation();

		// Create actor in this location
		final EventQueue queue = new EventQueue();
		actor = mock(Entity.class);
		when(actor.getParent()).thenReturn(loc);
		when(actor.getLocation()).thenReturn(loc);
		when(actor.getStance()).thenReturn(Stance.DEFAULT);
		when(actor.getGroup()).thenReturn(Optional.empty());
		when(actor.getFollowers()).thenReturn(Stream.empty());
		when(actor.getEventQueue()).thenReturn(queue);

		// Create inventory
		final Contents inv = new Contents();
		when(actor.getContents()).thenReturn(inv);
	}

	/**
	 * Registers an expected {@link ActionException} with the given reason
	 * @param reason Reason identifier
	 */
	protected void expect(String reason) {
		expected.expect(ActionException.class);
		expected.expectMessage(reason);
	}

	/**
	 * @return Mock location
	 */
	public static Location createLocation() {
		return new Location("loc", Area.ROOT, Terrain.DESERT, Collections.emptySet(), Collections.emptyList());
	}

	/**
	 * @return Mock parent/contents
	 */
	public static Parent createParent() {
		final Contents contents = new Contents();
		final Parent parent = mock(Parent.class);
		when(parent.getContents()).thenReturn(contents);
		return parent;
	}
}
