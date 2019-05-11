package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.View;
import org.sarge.textrpg.util.Description;

public class WindowTest {
	private Window window;

	@BeforeEach
	public void before() {
		window = new Window.Descriptor(new ObjectDescriptor.Builder("window").reset(Duration.ofMinutes(42)).build(), "curtains", View.of("view")).create();
	}

	@Test
	public void constructor() {
		assertNotNull(window);
		assertEquals("window", window.name());
		assertEquals("curtains", window.descriptor().drape());
		assertEquals("window", window.key(false));
		assertNotNull(window.view());
		assertNotNull(window.model());
		assertEquals(false, window.model().isOpen());
		assertEquals(false, window.model().isLockable());
	}

	@Test
	public void describe() {
		final Description expected = new Description.Builder("key").add("window.closed", "curtains").build();
		final Description.Builder builder = new Description.Builder("key");
		window.describe(false, builder, null);
		assertEquals(expected, builder.build());
	}

	@Test
	public void view() {
		window.model().set(Openable.State.OPEN);
		assertEquals("view", window.view().describe(null));
	}

	@Test
	public void viewClosed() {
		assertEquals(View.NONE, window.view());
	}
}
