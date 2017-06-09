package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;

public class PortalTest {
	private Portal portal;
	private Parent dest;
	
	@Before
	public void before() {
		dest = mock(Parent.class);
		portal = new Portal(new Portal.Descriptor(new ObjectDescriptor.Builder("portal").reset(42).build(), Openable.UNLOCKABLE), dest);
	}
	
	@Test
	public void constructor() {
		assertEquals(true, portal.getOpenableModel().isPresent());
		assertEquals(true, portal.isFixture());
		assertEquals(dest, portal.getDestination());
	}
	
	@Test
	public void describe() {
		final Description desc = portal.describe();
		assertEquals("{portal}", desc.get("name"));
		assertEquals(null, desc.get("open"));
	}
	
	@Test(expected = RuntimeException.class)
	public void destroy() {
		portal.destroy();
	}
}
