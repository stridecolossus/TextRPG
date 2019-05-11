package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;

public class OpenableContainerTest {
	private OpenableContainer container;

	@BeforeEach
	public void before() {
		final var descriptor = new ObjectDescriptor.Builder("container").reset(Duration.ofMillis(1)).build();
		container = new OpenableContainer(new OpenableContainer.Descriptor(descriptor, "in", LimitsMap.EMPTY, Openable.Lock.DEFAULT));
	}

	@Test
	public void constructor() {
		assertEquals("container", container.name());
		assertNotNull(container.descriptor());
		assertNotNull(container.contents());
		assertEquals(0, container.weight());
		assertNotNull(container.model());
		assertEquals(false, container.isOpen());
		assertEquals(Contents.EnumerationPolicy.CLOSED, container.contents().policy());
	}

	@Test
	public void reason() {
		assertEquals(Optional.of("container.closed"), container.contents().reason(null));
	}

	@Test
	public void open() {
		container.model().set(Openable.State.OPEN);
		assertEquals(true, container.isOpen());
		assertEquals(Contents.EnumerationPolicy.DEFAULT, container.contents().policy());
	}
}
