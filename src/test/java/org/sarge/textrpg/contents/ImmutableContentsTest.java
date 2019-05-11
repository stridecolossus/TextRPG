package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.ActionException;

public class ImmutableContentsTest {
	private Contents contents;
	private Thing thing;

	@BeforeEach
	public void before() {
		thing = mock(Thing.class);
		when(thing.weight()).thenReturn(42);
		contents = new ImmutableContents(true, List.of(thing));
	}

	@Test
	public void constructor() {
		assertEquals(true, contents.isRemoveAllowed());
		assertEquals(true, contents.contains(thing));
		assertEquals(42, contents.weight());
	}

	@Test
	public void reason() {
		assertEquals(Optional.of("contents.add.immutable"), contents.reason(thing));
	}

	@Test
	public void removeAllowed() {
		contents.remove(thing);
		assertEquals(false, contents.contains(thing));
		assertEquals(0, contents.weight());
	}

	@Test
	public void removeCannotRemove() throws ActionException {
		contents = new ImmutableContents(true, List.of(thing));
		contents.remove(thing);
	}
}
