package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.util.Percentile;

public class LampTest {
	private Lamp lamp;

	@BeforeEach
	public void before() {
		lamp = new Lamp(ObjectDescriptor.fixture("lamp"));
	}

	@Test
	public void constructor() {
		assertEquals("lamp", lamp.name());
		assertEquals(true, lamp.descriptor().isFixture());
		assertEquals(Percentile.ZERO, lamp.emission(Emission.LIGHT));
	}

	@Test
	public void setActive() {
		Lamp.setActive(true);
		assertEquals(Percentile.ONE, lamp.emission(Emission.LIGHT));
	}

	@Test
	public void listener() {
		final var parent = mock(Parent.class);
		when(parent.contents()).thenReturn(new Contents());
		lamp.parent(parent);
		Lamp.setActive(true);
		verify(parent).notify(ContentStateChange.LIGHT_MODIFIED);
	}
}
