package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.Contents.EnumerationPolicy;

public class ContentsTest {
	private Contents contents;
	private Thing thing;

	@BeforeEach
	public void before() {
		contents = new Contents();
		thing = new Thing() {
			@Override
			public String name() {
				return "thing";
			}
		};
	}

	@Test
	public void constructor() {
		assertEquals(EnumerationPolicy.DEFAULT, contents.policy());
		assertEquals("in", contents.placement());
		assertEquals(0, contents.size());
		assertEquals(true, contents.isEmpty());
		assertEquals(0, contents.weight());
		assertNotNull(contents.stream().count());
		assertEquals(0, contents.stream().count());
		assertEquals(false, contents.contains(thing));
	}

	@Test
	public void reason() {
		assertEquals(Optional.empty(), contents.reason(thing));
	}

	@Test
	public void reasonCannotRemove() {
		final Parent parent = mock(Parent.class);
		contents = new Contents() {
			@Override
			public boolean isRemoveAllowed() {
				return false;
			}
		};
		when(parent.contents()).thenReturn(contents);
		thing.parent(parent);
		assertEquals(Optional.of("contents.remove.immutable"), contents.reason(thing));
	}

	@Test
	public void add() {
		contents.add(thing);
		assertEquals(1, contents.size());
		assertEquals(1, contents.stream().count());
		assertEquals(thing, contents.stream().iterator().next());
		assertEquals(true, contents.contains(thing));
	}

	@Test
	public void remove() {
		contents.add(thing);
		contents.remove(thing);
		assertEquals(0, contents.size());
	}

	@Test
	public void removeLimbo() {
		thing.parent().contents().remove(thing);
	}

	@Test
	public void move() {
		// Create parent
		final Parent parent = mock(Parent.class);
		when(parent.contents()).thenReturn(contents);
		thing.parent(parent);

		// Create parent to move to
		final Parent other = mock(Parent.class);
		when(other.contents()).thenReturn(new Contents());

		// Move contents
		contents.move(other);
		assertEquals(other, thing.parent());
		assertEquals(true, contents.isEmpty());
	}

	@Test
	public void destroy() {
		final Parent parent = mock(Parent.class);
		when(parent.contents()).thenReturn(contents);
		thing.parent(parent);
		contents.destroy();
		assertEquals(true, contents.isEmpty());
	}
}
