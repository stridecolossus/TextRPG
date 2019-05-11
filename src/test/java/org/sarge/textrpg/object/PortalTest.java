package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Openable.OpenableException;
import org.sarge.textrpg.object.Portal.PortalState;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class PortalTest {
	private Portal portal;

	@BeforeEach
	public void before() {
		final Portal.Descriptor descriptor = new Portal.Descriptor(new ObjectDescriptor.Builder("portal").reset(Duration.ofMillis(1)).build(), Openable.Lock.DEFAULT);
		portal = new Portal(descriptor);
	}

	@Test
	public void constructor() {
		assertEquals("portal", portal.name());
		assertNotNull(portal.descriptor());
		assertEquals(false, portal.isQuiet());
		assertEquals(true, portal.descriptor().isResetable());
		assertEquals(true, portal.descriptor().isFixture());
		assertNotNull(portal.model());
		assertEquals(false, portal.model().isOpen());
		assertEquals(PortalState.DEFAULT, portal.state());
	}

	@Test
	public void describeClosed() throws ActionException {
		assertEquals("portal.closed", portal.describe(null).get(WorldObject.KEY_STATE).argument());
	}

	@Test
	public void describeOpen() {
		portal.model().set(Openable.State.OPEN);
		assertEquals("", portal.describe(null).get(WorldObject.KEY_STATE).argument());
	}

	@Test
	public void describeBroken() {
		portal.destroy();
		assertEquals("portal.broken", portal.describe(null).get(WorldObject.KEY_STATE).argument());
	}

	@Test
	public void block() {
		portal.block();
		assertEquals(PortalState.BLOCKED, portal.state());
		assertEquals(false, portal.model().isOpen());
	}

	@Test
	public void blockAlreadyBlocked() {
		portal.block();
		assertThrows(IllegalStateException.class, portal::block);
	}

	@Test
	public void destroy() {
		portal.destroy();
		assertEquals(PortalState.BROKEN, portal.state());
		assertEquals(true, portal.model().isOpen());
	}

	@Test
	public void open() throws ActionException {
		portal.model().apply(Openable.Operation.OPEN);
		assertEquals(true, portal.model().isOpen());
	}

	@Test
	public void openBlocked() throws ActionException {
		portal.block();
		TestHelper.expect(OpenableException.class, "portal.blocked", () -> portal.model().apply(Openable.Operation.OPEN));
	}

	@Test
	public void reset() {
		portal.model().set(Openable.State.OPEN);
		portal.block();
		portal.model().reset();
		assertEquals(false, portal.model().isOpen());
	}
}
