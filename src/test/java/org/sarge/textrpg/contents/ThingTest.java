package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;

public class ThingTest {
	private Thing thing;
	private Parent parent;
	private Contents contents;

	@BeforeEach
	public void before() {
		thing = new Thing() {
			@Override
			public String name() {
				return "thing";
			}
		};
		parent = mock(Parent.class);
		contents = mock(Contents.class);
		when(parent.contents()).thenReturn(contents);
	}

	@Test
	public void constructor() {
		assertEquals(0, thing.weight());
		assertEquals(Size.NONE, thing.size());
		assertEquals(Percentile.ONE, thing.visibility());
		assertEquals(false, thing.isAlive());
		assertEquals(false, thing.isQuiet());
		assertEquals(Parent.LIMBO, thing.parent());
		assertEquals(Percentile.ZERO, thing.emission(Emission.LIGHT));
	}

	@Test
	public void parent() {
		thing.parent(parent);
		assertEquals(parent, thing.parent());
		verify(contents).add(thing);
	}

	@Test
	public void parentDuplicate() {
		thing.parent(parent);
		assertThrows(IllegalArgumentException.class, () -> thing.parent(parent));
	}

	@Test
	public void parentLimbo() {
		assertThrows(IllegalArgumentException.class, () -> thing.parent(Parent.LIMBO));
	}

	@Test
	public void destroy() {
		thing.parent(parent);
		thing.destroy();
		assertEquals(Parent.LIMBO, thing.parent());
		verify(contents).remove(thing);
	}

	@Test
	public void destroyNotActive() {
		assertThrows(IllegalStateException.class, () -> thing.destroy());
	}

	@Test
	public void destroyAlreadyDestroyed() {
		thing.parent(parent);
		thing.destroy();
		assertThrows(IllegalStateException.class, () -> thing.destroy());
	}

	@Test
	public void raise() {
		final ContentStateChange notification = ContentStateChange.of(ContentStateChange.Type.OTHER, new Description("key"));
		thing.raise(notification);
		thing.parent(parent);
		thing.raise(notification);
		verify(parent).notify(notification);
	}

	@Test
	public void maxZero() {
		assertEquals(Percentile.ZERO, Thing.max(Emission.LIGHT, Stream.of(thing)));
	}

	@Test
	public void max() {
		final Thing half = mock(Thing.class);
		final Thing one = mock(Thing.class);
		when(half.emission(Emission.LIGHT)).thenReturn(Percentile.HALF);
		when(one.emission(Emission.LIGHT)).thenReturn(Percentile.ONE);
		assertEquals(Percentile.ONE, Thing.max(Emission.LIGHT, Stream.of(half, thing, one, half, thing, one)));
	}
}
