package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Percentile;

public class HiddenLinkTest {
	private HiddenLink link;

	@BeforeEach
	public void before() {
		link = new HiddenLink(HiddenLink.DEFAULT_PROPERTIES, "name", Percentile.HALF);
	}

	@Test
	public void constructor() {
		assertEquals(false, link.isQuiet());
		assertEquals("name", link.name());
		assertNotNull(link.controller());
		assertEquals(true, link.controller().isPresent());
	}

	@Test
	public void controller() {
		final var controller = link.controller().get();
		assertEquals(Percentile.HALF, controller.visibility());
	}
}
