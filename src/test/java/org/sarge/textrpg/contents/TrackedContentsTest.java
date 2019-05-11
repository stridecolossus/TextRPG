package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TrackedContentsTest {
	private TrackedContents contents;
	private Thing thing;

	@BeforeEach
	public void before() {
		contents = new TrackedContents();
		thing = mock(Thing.class);
		when(thing.parent()).thenReturn(Parent.LIMBO);
		when(thing.weight()).thenReturn(42);
	}

	@Test
	public void constructor() {
		assertEquals(0, contents.weight());
	}

	@Test
	public void add() {
		contents.add(thing);
		assertEquals(42, contents.weight());
	}

	@Test
	public void remove() {
		contents.add(thing);
		contents.remove(thing);
		assertEquals(0, contents.weight());
	}

	@Test
	public void update() {
		contents.add(thing);
		when(thing.weight()).thenReturn(999);
		contents.update();
		assertEquals(999, contents.weight());
	}
}
