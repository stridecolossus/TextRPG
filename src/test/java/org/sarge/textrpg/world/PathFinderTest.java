package org.sarge.textrpg.world;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.util.DataTableCalculator;

public class PathFinderTest {
	private PathFinder finder;
	private Actor actor;
	private Location start, end;
	private DataTableCalculator move;
	private Optional<Iterator<Direction>> result;

	@Before
	public void before() {
		move = mock(DataTableCalculator.class);
		when(move.multiply(any())).thenReturn(1f);
		finder = new PathFinder(move);
		start = create("start");
		end = create("end");
		actor = mock(Actor.class);
	}

	private static Location create(String name) {
		return new Location(name, Area.ROOT, Terrain.DESERT, Collections.emptySet(), Collections.emptyList());
	}

	private void run() {
		result = finder.build(start, end, actor);
	}

	@Test
	public void cannotFindPath() {
		run();
		assertEquals(Optional.empty(), result);
	}

	@Test
	public void cannotTraverseLink() {
		// Add a link that cannot be traversed
		final Link closed = mock(Link.class);
		when(closed.reason(actor)).thenReturn("closed");
		start.add(new LinkWrapper(Direction.EAST, closed, end));

		// Add a link that is not visible to the actor
		final Link invisible = mock(Link.class);
		when(invisible.isVisible(actor)).thenReturn(false);
		start.add(new LinkWrapper(Direction.WEST, invisible, end));

		// Check cannot find path
		run();
		assertEquals(Optional.empty(), result);
	}

	@Test
	public void validPath() {
		// Add a link
		start.add(new LinkWrapper(Direction.EAST, Link.DEFAULT, end));
		when(actor.perceives(any(Hidden.class))).thenReturn(true);

		// Check path found
		run();
		assertEquals(true, result.isPresent());

		// Check path
		final Iterator<Direction> itr = result.get();
		assertEquals(true, itr.hasNext());
		assertEquals(Direction.EAST, itr.next());
		assertEquals(false, itr.hasNext());
	}

	@Test
	public void cheapestPath() {
		// TODO
	}

	@Test(expected = IllegalArgumentException.class)
	public void sameStartEnd() {
		finder.build(start, start, actor);
	}
}
