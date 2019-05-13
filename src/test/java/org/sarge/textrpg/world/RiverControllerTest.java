package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.CurrentLink.Current;

public class RiverControllerTest {
	private RiverController controller;
	private MovementController mover;
	private Event.Queue queue;
	private Thing thing;
	private Location loc;
	private Exit exit;

	@BeforeEach
	public void before() {
		// Create controller
		queue = new Event.Queue.Manager().queue("current");
		mover = mock(MovementController.class);
		controller = new RiverController(queue, mover);
		controller.setMovementPeriod(Duration.ofSeconds(1));

		// Create exit
		loc = mock(Location.class);
		when(loc.isWater()).thenReturn(true);
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		exit = Exit.of(Direction.EAST, new CurrentLink(Current.MEDIUM), loc);

		// Create entity to move
		thing = mock(Entity.class);
		when(thing.parent()).thenReturn(loc);
	}

	@DisplayName("Add an object and register current event")
	@Test
	public void add() {
		controller.add(thing, exit);
		assertEquals(1, queue.size());
	}

	@DisplayName("Current carries along entity")
	@Test
	public void moveEntity() {
		controller.add(thing, exit);
		queue.manager().advance(Duration.ofSeconds(2).toMillis());
		verify(mover).notify((Entity) thing, exit, loc);
	}

	@DisplayName("Current carries along floating object")
	@Test
	public void moveObject() {
		thing = mock(Thing.class);
		when(thing.name()).thenReturn("object");
		controller.add(thing, exit);
		queue.manager().advance(Duration.ofSeconds(2).toMillis());
		verifyZeroInteractions(mover);
		verify(thing).parent(loc);
		verify(loc).broadcast(null, new Description("river.object.moved", "object"));
	}

	@DisplayName("Object that has been moved in the meantime is ignored")
	@Test
	public void moveParentChanged() {
		controller.add(thing, exit);
		when(thing.parent()).thenReturn(TestHelper.parent());
		queue.manager().advance(Duration.ofSeconds(2).toMillis());
		verifyZeroInteractions(mover);
	}

	@DisplayName("Object moved into another current is re-registered")
	@Test
	public void moveIntoCurrent() {
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		controller.add(thing, exit);
		queue.manager().advance(Duration.ofSeconds(2).toMillis());
		assertEquals(1, queue.size());
	}

	@Test
	public void addNotRiverCurrent() {
		assertThrows(ClassCastException.class, () -> controller.add(thing, Exit.of(Direction.EAST, loc)));
	}
}
