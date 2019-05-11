package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;

public class PortalLinkTest {
	private Portal portal;
	private PortalLink link;
	private Thing actor;

	@BeforeEach
	public void before() {
		// Create portal descriptor
		final var descriptor = new ObjectDescriptor.Builder("portal").reset(Duration.ofSeconds(1)).quiet(true).build();
		portal = new Portal(new Portal.Descriptor(descriptor, Openable.Lock.DEFAULT));

		// Create link
		link = new PortalLink(PortalLink.DEFAULT_PROPERTIES, portal);

		// Create actor
		actor = mock(Thing.class);
		when(actor.size()).thenReturn(Size.NONE);
	}

	@Test
	public void constructor() {
		assertEquals(Optional.of(portal), link.controller());
		assertEquals(true, link.isQuiet());
		assertEquals(false, link.isTraversable());
		assertEquals(false, link.isEntityOnly());
		assertEquals(Optional.of(new Description("portal.closed", "portal")), link.reason(actor));
		assertEquals("[dir]", link.wrap("dir"));
	}

	@Test
	public void open() {
		portal.model().set(Openable.State.OPEN);
		assertEquals(true, link.isTraversable());
		assertEquals(Optional.empty(), link.reason(actor));
		assertEquals("default", link.key());
		assertEquals("(dir)", link.wrap("dir"));
	}

	@Test
	public void closed() {
		assertEquals(false, link.isTraversable());
		assertEquals(Optional.of(new Description("portal.closed", "portal")), link.reason(actor));
		assertEquals("closed", link.key());
		assertEquals("[dir]", link.wrap("dir"));
	}

	@Test
	public void blocked() {
		portal.block();
		assertEquals(false, link.isTraversable());
		assertEquals(Optional.of(new Description("portal.blocked", "portal")), link.reason(actor));
		assertEquals("BLOCKED", link.key());
		assertEquals("!dir!", link.wrap("dir"));
	}

	@Test
	public void broken() {
		portal.destroy();
		assertEquals(true, link.isTraversable());
		assertEquals(Optional.empty(), link.reason(actor));
		assertEquals("BROKEN", link.key());
		assertEquals("#dir#", link.wrap("dir"));
	}
}
