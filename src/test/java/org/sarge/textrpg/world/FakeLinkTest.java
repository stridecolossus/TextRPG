package org.sarge.textrpg.world;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.textrpg.common.Size;

public class FakeLinkTest {
	@Test
	public void constructor() {
		final Link link = new FakeLink(Route.CORRIDOR, Size.NONE, "dest", "message");
		assertEquals(false, link.isTraversable(null));
		assertEquals("message", link.getReason());
		assertEquals("dest", link.getDestinationName(null));
	}
}
