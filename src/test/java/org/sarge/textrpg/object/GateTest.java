package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.object.Gate.Keeper;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Link;

public class GateTest {
	private Gate gate;

	@BeforeEach
	public void before() {
		final Faction faction = mock(Faction.class);
		final var descriptor = new ObjectDescriptor.Builder("gate").reset(Duration.ofMillis(1)).build();
		gate = new Gate(new Gate.Descriptor(descriptor, new Keeper("harry", new Faction.Association(faction, Relationship.FRIENDLY), 42)));
	}

	@Test
	public void constructor() {
		assertEquals("gate", gate.name());
		assertEquals(true, gate.isQuiet());
		assertNotNull(gate.descriptor());
		assertEquals(false, gate.isOpen());
	}

	@Test
	public void keeper() {
		assertNotNull(gate.descriptor().keeper());
		assertTrue(gate.descriptor().keeper().isPresent());
		final Keeper keeper = gate.descriptor().keeper().get();
		assertEquals("harry", keeper.name());
		assertNotNull(keeper.association());
		assertEquals(Optional.of(42), keeper.bribe());
	}

	@Test
	public void open() {
		gate.descriptor().setOpen(true);
		assertEquals(true, gate.isOpen());
	}

	@Test
	public void callOpen() throws ActionException {
		gate.call();
		assertEquals(true, gate.isOpen());
	}

	@Test
	public void reset() throws ActionException {
		gate.call();
		gate.reset();
		assertEquals(false, gate.isOpen());
	}

	@Test
	public void callClose() throws ActionException {
		gate.call();
		gate.call();
		assertEquals(false, gate.isOpen());
	}

	@Test
	public void callOpenDaytime() throws ActionException {
		gate.descriptor().setOpen(true);
		TestHelper.expect("gate.call.daytime", gate::call);
	}

	@Test
	public void callNoGateKeeper() throws ActionException {
		final var descriptor = new ObjectDescriptor.Builder("gate").reset(Duration.ofMillis(1)).build();
		gate = new Gate(new Gate.Descriptor(descriptor, null));
		assertEquals(Optional.empty(), gate.descriptor().keeper());
		assertThrows(IllegalStateException.class, gate::call);
	}

	@Test
	public void link() {
		// Check closed gate
		final Link link = gate.link();
		assertNotNull(link);
		assertEquals(false, link.isTraversable());
		assertEquals(false, link.isEntityOnly());
		assertEquals(false, link.isQuiet());
		assertEquals(Optional.of(new Description("portal.closed", "gate")), link.reason(null));
		assertEquals("[dir]", link.wrap("dir"));

		// Check open gate
		gate.descriptor().setOpen(true);
		assertEquals(true, link.isTraversable());
		assertEquals(Optional.empty(), link.reason(null));
		assertEquals("(dir)", link.wrap("dir"));
	}
}
