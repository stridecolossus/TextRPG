package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Link;

public class BlockedLinkTest {
	private Link link;
	private WorldObject blockage;

	@BeforeEach
	public void before() {
		blockage = ObjectDescriptor.of("blockage").create();
		link = new BlockedLink(BlockedLink.DEFAULT_PROPERTIES, blockage);
	}

	@Test
	public void constructor() {
		blockage.parent(TestHelper.parent());
		assertEquals(Optional.of(blockage), link.controller());
		assertEquals(Optional.of(new Description("link.blocked", "blockage")), link.reason(null));
		assertEquals(false, link.isQuiet());
		assertEquals(true, link.isTraversable());
		assertEquals(false, link.isEntityOnly());
		assertEquals("#dir#", link.wrap("dir"));
	}

	@Test
	public void unblock() {
		assertEquals(Optional.empty(), link.controller());
		assertEquals(true, link.isTraversable());
		assertEquals(Optional.empty(), link.reason(null));
		assertEquals("dir", link.wrap("dir"));
	}
}
