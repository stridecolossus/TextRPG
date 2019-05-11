package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;

public class FakeLinkTest {
	private Link link;

	@BeforeEach
	public void before() {
		link = new FakeLink("name", "reason");
	}

	@Test
	public void constructor() {
		assertEquals("name", link.name(null));
		assertEquals(Optional.of(new Description("reason")), link.reason(null));
		assertEquals(false, link.isTraversable());
		assertEquals(Optional.empty(), link.controller());
	}

	@Test
	public void invert() {
		assertThrows(UnsupportedOperationException.class, () -> link.invert());
	}
}
