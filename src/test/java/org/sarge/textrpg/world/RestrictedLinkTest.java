package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.ExtendedLink.Properties;

public class RestrictedLinkTest {
	private Link link;

	@BeforeEach
	public void before() {
		link = new ExtendedLink(new Properties(Size.MEDIUM, Route.LANE, 2, "message"));
	}

	@Test
	public void constructor() {
		assertEquals(Size.MEDIUM, link.size());
		assertEquals(Route.LANE, link.route());
		assertEquals(2f, link.modifier(), 0.0001f);
		assertEquals(Optional.of(Description.of("message")), link.message());
		assertEquals(Optional.empty(), link.controller());
		assertEquals(false, link.isQuiet());
		assertEquals(true, link.isTraversable());
		assertEquals(false, link.isEntityOnly());
		assertNotNull(link.controller());
		assertEquals(false, link.controller().isPresent());
	}

	@Test
	public void reason() {
		// Check smaller actor
		final Thing actor = mock(Thing.class);
		when(actor.size()).thenReturn(Size.MEDIUM);
		assertEquals(Optional.empty(), link.reason(actor));

		// Check large actor
		when(actor.size()).thenReturn(Size.LARGE);
		assertEquals(Optional.of(new Description("link.too.small")), link.reason(actor));
	}
}
