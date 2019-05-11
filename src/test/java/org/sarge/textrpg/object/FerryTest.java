package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.collection.LoopIterator;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.entity.Ferry;
import org.sarge.textrpg.entity.Ferry.Ticket;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.DefaultLocation;
import org.sarge.textrpg.world.Location;

public class FerryTest extends ActionTestBase {
	private Ferry ferry;
	private Location one, two;

	@BeforeEach
	public void before() {
		one = create("one");
		two = create("two");
		ferry = new Ferry("ferry", List.of(one, two), true, LimitsMap.EMPTY);
	}

	private static Location create(String name) {
		return new DefaultLocation(new Location.Descriptor(name), Area.ROOT);
	}

	@Test
	public void constructor() {
		assertEquals(one, ferry.parent());
		assertNotNull(ferry.contents());
		assertEquals(true, ferry.isTicketRequired());
	}

	@Test
	public void ticket() {
		final Ticket ticket = ferry.new Ticket(one, 42);
		assertEquals(one, ticket.destination());
		assertEquals("ferry.ticket.one", ticket.name());
		assertEquals(42, ticket.value());
	}

	@Test
	public void ticketInvalidLocation() {
		final Location invalid = mock(Location.class);
		when(invalid.name()).thenReturn("invalid");
		assertThrows(IllegalArgumentException.class, () -> ferry.new Ticket(invalid, 42));
	}

	@Test
	public void iterator() {
		assertNotNull(ferry.iterator(LoopIterator.Strategy.CYCLE));
	}

	@Test
	public void constructorInsufficientWaypoints() {
		assertThrows(IllegalArgumentException.class, () -> new Ferry("ferry", List.of(one), true, LimitsMap.EMPTY));
	}

	@Test
	public void constructorTicketNotRequired() {
		ferry = new Ferry("ferry", List.of(one, two), false, LimitsMap.EMPTY);
		assertThrows(IllegalStateException.class, () -> ferry.new Ticket(one, 42));
	}

	@Test
	public void move() {
		ferry.move(two);
		assertEquals(two, ferry.parent());
	}

	@Test
	public void moveSameWayPoint() {
		assertThrows(IllegalArgumentException.class, () -> ferry.move(one));
	}

	@Test
	public void parent() {
		assertThrows(IllegalStateException.class, () -> ferry.parent(two));
	}
}
