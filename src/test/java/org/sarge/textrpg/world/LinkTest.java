package org.sarge.textrpg.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Portal;
import org.sarge.textrpg.object.PortalLink;
import org.sarge.textrpg.util.Percentile;

public class LinkTest {
	private Actor actor;

	@Before
	public void before() {
		actor = mock(Actor.class);
		when(actor.size()).thenReturn(Size.MEDIUM);
	}

	@Test
	public void simpleLink() {
		final Link link = Link.DEFAULT;
		assertEquals(Route.NONE, link.route());
		assertEquals(Script.NONE, link.script());
		assertNotNull(link.controller());
		assertEquals(true, link.isVisible(actor));
		assertEquals(true, link.isTraversable(actor));
		assertEquals(Optional.empty(), link.controller());
		assertEquals("dir", link.describe("dir"));
		assertNotNull(link.describe());
	}

	@Test
	public void routeLink() {
		final Link link = new RouteLink(Route.BRIDGE);
		assertEquals(Route.BRIDGE, link.route());
		assertEquals(Script.NONE, link.script());
		assertNotNull(link.controller());
		assertEquals(true, link.isVisible(actor));
		assertEquals(true, link.isTraversable(actor));
		assertEquals(Optional.empty(), link.controller());
		assertEquals("(dir(", link.describe("dir"));
		assertNotNull(link.describe());
	}

	@Test
	public void hiddenLink() {
		final Link link = new HiddenLink(Route.ROAD, Script.NONE, Size.NONE, "name", Percentile.HALF, 42);
		assertEquals(Route.ROAD, link.route());
		assertEquals(Script.NONE, link.script());
		assertNotNull(link.controller());
		assertEquals(false, link.isVisible(actor));
		assertEquals(true, link.isTraversable(actor));
		assertNotNull(link.controller());
		assertEquals(true, link.controller().isPresent());
		assertEquals(Percentile.HALF, link.controller().get().visibility());
		assertEquals("=dir=", link.describe("dir"));
		assertNotNull(link.describe());
	}

	@Test
	public void revealedLink() {
		final Link link = new HiddenLink(Route.ROAD, Script.NONE, Size.NONE, "name", Percentile.HALF, 42);
		when(actor.perceives(link.controller().get())).thenReturn(true);
		assertEquals(true, link.isVisible(actor));
	}

	@Test
	public void portalLink() {
		// Create portal
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("door").reset(42).build();
		final Portal portal = new Portal(new Portal.Descriptor(descriptor, Openable.UNLOCKABLE), mock(Parent.class));

		// Create link and check controlled by the object
		final Link link = new PortalLink(Route.NONE, Script.NONE, Size.NONE, portal);
		assertEquals(Route.NONE, link.route());
		assertEquals(Script.NONE, link.script());
		assertNotNull(link.controller());
		assertEquals(false, link.isVisible(actor));
		assertEquals(false, link.isTraversable(actor));
		assertEquals(Optional.of(portal), link.controller());
		assertEquals("[dir]", link.describe("dir"));

		// Check full description
		final Description desc = link.describe().build();
		assertEquals("exit.closed", desc.getKey());
		assertEquals("{door}", desc.get("name"));

		// Open the portal and check can now be traversed
		portal.openableModel().get().setOpen(true);
		assertEquals(true, link.isTraversable(actor));
		assertEquals("(dir)", link.describe("dir"));
		assertEquals("exit.entry", link.describe().build().getKey());
		assertEquals(null, link.describe().build().get("closed"));
	}

	@Test
	public void extendedLink() {
		final Script script = mock(Script.class);
		final Link link = new ExtendedLink(Route.NONE, script, Size.MEDIUM);
		assertEquals(Route.NONE, link.route());
		assertEquals(script, link.script());
		assertEquals(true, link.isVisible(actor));
		assertNotNull(link.controller());
		assertEquals(Optional.empty(), link.controller());
		assertEquals("dir", link.describe("dir"));
		assertNotNull(link.describe());

		final Actor actor = mock(Actor.class);
		when(actor.size()).thenReturn(Size.SMALL);
		assertEquals(true, link.isTraversable(actor));

		when(actor.size()).thenReturn(Size.LARGE);
		assertEquals(false, link.isTraversable(actor));
	}
}
