package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;

public class SlopeLinkTest {
	private SlopeLink link;

	@BeforeEach
	public void before() {
		link = new SlopeLink(SlopeLink.DEFAULT_PROPERTIES, true);
	}

	@Test
	public void constructor() {
		assertEquals("/dir/", link.wrap("dir"));
		assertEquals(true, link.isTraversable());
		assertEquals(false, link.isQuiet());
		assertEquals(false, link.isEntityOnly());
		assertEquals(true, link.up());
	}

	@Test
	public void describe() {
		final var description = new Description.Builder("key");
		link.describe(description);
		assertNotNull(description.get("slope.direction"));
	}

	@Test
	public void invert() {
		final Link inverted = link.invert();
		assertEquals("\\dir\\", inverted.wrap("dir"));
	}
}
