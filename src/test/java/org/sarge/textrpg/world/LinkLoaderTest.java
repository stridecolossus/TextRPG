package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.Element.ElementException;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.OpeningTimes;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.object.BlockedLink;
import org.sarge.textrpg.object.ClimbLink;
import org.sarge.textrpg.object.ContainerLink;
import org.sarge.textrpg.object.DefaultObjectDescriptorLoader;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptorLoader;
import org.sarge.textrpg.object.Portal;
import org.sarge.textrpg.object.PortalLink;
import org.sarge.textrpg.object.Rope.RopeLink;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.PeriodModel;

public class LinkLoaderTest {
	private static final Duration RESET = Duration.ofMinutes(1);

	private DefaultObjectDescriptorLoader descriptorLoader;
	private LinkLoader loader;
	private Parent parent;
	private LoaderContext ctx;

	@BeforeEach
	public void before() {
		// Create loader
		descriptorLoader = mock(DefaultObjectDescriptorLoader.class);
		loader = new LinkLoader(descriptorLoader);

		// Create parent area for link controllers
		parent = mock(Parent.class);
		when(parent.contents()).thenReturn(new Contents());

		// Init context
		ctx = mock(LoaderContext.class);
		when(ctx.route()).thenReturn(Route.NONE);
		when(ctx.terrain()).thenReturn(Terrain.DESERT);
	}

	@Test
	public void loadDefault() {
		assertEquals(Link.DEFAULT, loader.load(Element.of("link"), null, ctx));
	}

	@Test
	public void loadRoute() {
		final Element xml = new Element.Builder("route").attribute("route", "lane").build();
		assertEquals(RouteLink.of(Route.LANE), loader.load(xml, null, ctx));
	}

	@Test
	public void loadExtended() {
		// Build extended link XML
		final Element xml = new Element.Builder("extended")
			.attribute("size", "medium")
			.attribute("mod", 2)
			.attribute("message", "message")
			.build();

		// Load restricted link
		final Link link = loader.load(xml, null, ctx);
		assertNotNull(link);
		assertTrue(link instanceof ExtendedLink);
		assertEquals(Size.MEDIUM, link.size());
		assertEquals(2f, link.modifier(), 0.0001f);
		assertEquals(Optional.of(Description.of("message")), link.message());
	}

	@Test
	public void loadHidden() {
		// Build hidden link XML
		final Element xml = new Element.Builder("hidden")
			.attribute("name", "name")
			.attribute("vis", 50)
			.build();

		// Load hidden link
		final Link link = loader.load(xml, null, ctx);
		assertNotNull(link);
		assertTrue(link instanceof HiddenLink);
		assertTrue(link.controller().isPresent());

		// Check controller
		final Thing controller = link.controller().get();
		assertEquals("name", controller.name());
		assertEquals(Percentile.HALF, controller.visibility());
	}

	@Test
	public void loadFake() {
		// Build fake link XML
		final Element xml = new Element.Builder("fake")
			.attribute("name", "name")
			.attribute("reason", "reason")
			.build();

		// Load fake link
		final Link link = loader.load(xml, null, ctx);
		assertNotNull(link);
		assertTrue(link instanceof FakeLink);
		assertEquals("name", link.name(null));
	}

	@Test
	public void loadPortal() {
		// Build portal link XML
		final Element xml = new Element.Builder("portal").add("portal").build();

		// Create portal
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("door").reset(RESET).build();
		final Portal.Descriptor portal = new Portal.Descriptor(descriptor, Openable.Lock.DEFAULT);
		when(descriptorLoader.get("door")).thenReturn(portal);

		// Load link
		final Link link = loader.load(xml, parent, ctx);
		assertNotNull(link);
		assertTrue(link instanceof PortalLink);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void loadGate() {
		// Build gate link XML
		final Element xml = new Element.Builder("gate")
			.attribute("descriptor", "gate")
			.attribute("faction", "faction")
			.build();

		// Register gate descriptor
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("gate").reset(RESET).build();
		when(descriptorLoader.get("gate")).thenReturn(descriptor);

		// Add faction
		final Faction faction = mock(Faction.class);
		when(faction.opening()).thenReturn(mock(PeriodModel.class));
		when(faction.opening().current()).thenReturn(new OpeningTimes(LocalTime.of(1, 2), true));
		when(ctx.faction()).thenReturn(Optional.of(faction));

		// Load gate link
		final Link link = loader.load(xml, parent, ctx);
		assertNotNull(link);
	}

	@Test
	public void loadSlope() {
		final Element xml = new Element.Builder("slope").attribute("up", true).build();
		final Link link = loader.load(xml, null, ctx);
		assertNotNull(link);
		assertTrue(link instanceof SlopeLink);
	}

	@Test
	public void loadClimb() {
		// Build climb link XML
		final Element xml = new Element.Builder("climb")
			.attribute("diff", 50)
			.attribute("mod", 2)
			.add("climb")
			.build();

		// Create climbable object
		when(descriptorLoader.load(xml.child("climb"), ObjectDescriptorLoader.Policy.FIXTURE)).thenReturn(ObjectDescriptor.fixture("climbable"));

		// Load link
		final Link link = loader.load(xml, parent, ctx);
		assertNotNull(link);
		assertTrue(link instanceof ClimbLink);
		assertEquals(parent, link.controller().get().parent());

		// Check link properties
		assertEquals(true, link.isQuiet());
		assertEquals(2f, link.modifier(), 0.0001f);

		// Check climb difficulty
		final ClimbLink climb = (ClimbLink) link;
		assertEquals(Percentile.HALF, climb.difficulty());
	}

	@Test
	public void loadRope() {
		// Build rope link XML
		final Element xml = new Element.Builder("rope").attribute("anchor", "anchor").build();

		// Load link
		final Link link = loader.load(xml, parent, ctx);
		assertNotNull(link);
		assertTrue(link instanceof RopeLink);
		assertEquals("anchor", link.controller().get().name());
		assertEquals(parent, link.controller().get().parent());
	}

	@Test
	public void loadBlocked() {
		// Build blocked link XML
		final Element xml = new Element.Builder("blocked").add("object").build();

		// Create blockage
		when(descriptorLoader.load(xml.child("object"), ObjectDescriptorLoader.Policy.FIXTURE)).thenReturn(ObjectDescriptor.fixture("blockage"));

		// Load link
		final Link link = loader.load(xml, parent, ctx);
		assertNotNull(link);
		assertTrue(link instanceof BlockedLink);
		assertEquals("blockage", link.controller().get().name());
		assertEquals(parent, link.controller().get().parent());
	}

	@Test
	public void loadContainer() {
		// Build container link XML
		final Element xml = new Element.Builder("container")
			.attribute("name", "name")
			.attribute("cat", "cat")
			.build();

		// Load link
		final Link link = loader.load(xml, null, ctx);
		assertNotNull(link);
		assertTrue(link instanceof ContainerLink);
	}

	@Test
	public void loadUnknown() {
		assertThrows(ElementException.class, () -> loader.load(Element.of("cobblers"), null, ctx));
	}
}
