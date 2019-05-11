package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.util.Description;

public class ContainerLinkTest {
	private ContainerLink link;
	private Parent parent;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		link = new ContainerLink(ContainerLink.DEFAULT_PROPERTIES, "link", "cat");
		parent = (Parent) link.controller().get();
		obj = new ObjectDescriptor.Builder("object").category("cat").build().create();
	}

	@Test
	public void constructor() {
		assertEquals(true, link.controller().isPresent());
		assertEquals(Optional.of(new Description("link.requires.object")), link.reason(null));
		assertEquals(true, link.isTraversable());
		assertEquals(false, link.isQuiet());
		assertEquals(true, link.isEntityOnly());
		assertEquals("!dir!", link.wrap("dir"));
	}

	@Test
	public void add() {
		// Add the required object
		assertEquals(Optional.empty(), parent.contents().reason(obj));
		obj.parent(parent);

		// Check link can now be traversed
		assertEquals(true, link.isTraversable());
		assertEquals(Optional.empty(), link.reason(null));
		assertEquals("dir", link.wrap("dir"));

		// Remove object
		obj.destroy();
	}

	@Test
	public void invalid() {
		final var invalid = ObjectDescriptor.of("invalid").create();
		assertEquals(Optional.of("link.invalid.object"), parent.contents().reason(invalid));
	}
}
