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
		when(actor.getSize()).thenReturn(Size.MEDIUM);
	}

	@Test
	public void simpleLink() {
		final Link link = Link.DEFAULT;
		assertEquals(Route.NONE, link.getRoute());
		assertEquals(Script.NONE, link.getScript());
		assertNotNull(link.getController());
		assertEquals(true, link.isVisible(actor));
		assertEquals(true, link.isTraversable(actor));
		assertEquals(Optional.empty(), link.getController());
		assertEquals("dir", link.describe("dir"));
		assertNotNull(link.describe());
	}

	@Test
	public void routeLink() {
		final Link link = new RouteLink(Route.BRIDGE);
		assertEquals(Route.BRIDGE, link.getRoute());
		assertEquals(Script.NONE, link.getScript());
		assertNotNull(link.getController());
		assertEquals(true, link.isVisible(actor));
		assertEquals(true, link.isTraversable(actor));
		assertEquals(Optional.empty(), link.getController());
		assertEquals("(dir(", link.describe("dir"));
		assertNotNull(link.describe());
	}

	@Test
	public void hiddenLink() {
		final Link link = new HiddenLink(Route.ROAD, Script.NONE, Size.NONE, "name", Percentile.HALF, 42);
		assertEquals(Route.ROAD, link.getRoute());
		assertEquals(Script.NONE, link.getScript());
		assertNotNull(link.getController());
		assertEquals(false, link.isVisible(actor));
		assertEquals(true, link.isTraversable(actor));
		assertNotNull(link.getController());
		assertEquals(true, link.getController().isPresent());
		assertEquals(Percentile.HALF, link.getController().get().getVisibility());
		assertEquals("=dir=", link.describe("dir"));
		assertNotNull(link.describe());
	}

	@Test
	public void revealedLink() {
		final Link link = new HiddenLink(Route.ROAD, Script.NONE, Size.NONE, "name", Percentile.HALF, 42);
		when(actor.perceives(link.getController().get())).thenReturn(true);
		assertEquals(true, link.isVisible(actor));
	}

	@Test
	public void portalLink() {
		// Create portal
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("door").reset(42).build();
		final Portal portal = new Portal(new Portal.Descriptor(descriptor, Openable.UNLOCKABLE), mock(Parent.class));

		// Create link and check controlled by the object
		final Link link = new PortalLink(Route.NONE, Script.NONE, Size.NONE, portal);
		assertEquals(Route.NONE, link.getRoute());
		assertEquals(Script.NONE, link.getScript());
		assertNotNull(link.getController());
		assertEquals(false, link.isVisible(actor));
		assertEquals(false, link.isTraversable(actor));
		assertEquals(Optional.of(portal), link.getController());
		assertEquals("[dir]", link.describe("dir"));

		// Check full description
		final Description desc = link.describe().build();
		assertEquals("exit.closed", desc.getKey());
		assertEquals("{door}", desc.get("name"));

		// Open the portal and check can now be traversed
		portal.getOpenableModel().get().setOpen(true);
		assertEquals(true, link.isTraversable(actor));
		assertEquals("(dir)", link.describe("dir"));
		assertEquals("exit.entry", link.describe().build().getKey());
		assertEquals(null, link.describe().build().get("closed"));
	}

	@Test
	public void extendedLink() {
		final Script script = mock(Script.class);
		final Link link = new ExtendedLink(Route.NONE, script, Size.MEDIUM);
		assertEquals(Route.NONE, link.getRoute());
		assertEquals(script, link.getScript());
		assertEquals(true, link.isVisible(actor));
		assertNotNull(link.getController());
		assertEquals(Optional.empty(), link.getController());
		assertEquals("dir", link.describe("dir"));
		assertNotNull(link.describe());

		final Actor actor = mock(Actor.class);
		when(actor.getSize()).thenReturn(Size.SMALL);
		assertEquals(true, link.isTraversable(actor));

		when(actor.getSize()).thenReturn(Size.LARGE);
		assertEquals(false, link.isTraversable(actor));
	}
}
