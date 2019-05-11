package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.ExtendedLink;

public class RopeLinkTest {
	private Link link;
	private Rope.Anchor anchor;

	@BeforeEach
	public void before() {
		anchor = mock(Rope.Anchor.class);
		link = new Rope.RopeLink(new ExtendedLink.Properties(), anchor);
	}

	@Test
	public void constructor() {
		assertEquals(false, link.isQuiet());
		assertEquals(false, link.isTraversable());
		assertEquals(true, link.isEntityOnly());
		assertEquals(Optional.of(anchor), link.controller());
	}

	@Test
	public void reason() {
		assertEquals(Optional.of(new Description("link.requires.rope")), link.reason(null));
		when(anchor.isAttached()).thenReturn(true);
		assertEquals(Optional.empty(), link.reason(null));
	}

	@Test
	public void wrap() {
		assertEquals("dir", link.wrap("dir"));
		when(anchor.isAttached()).thenReturn(true);
		assertEquals("|dir|", link.wrap("dir"));
	}
}
