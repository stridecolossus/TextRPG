package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;

public class GateTest {
	private Gate gate;

	@Before
	public void before() {
		gate = new Gate(new Portal.Descriptor(new ObjectDescriptor.Builder("gate").reset(1).build(), Openable.FIXED), mock(Parent.class));
	}

	@Test
	public void constructor() {
		assertNotNull(gate.openableModel());
		assertEquals(true, gate.openableModel().isPresent());
		assertEquals(false, gate.openableModel().get().isOpen());
		assertEquals(true, gate.isFixture());
	}

	@Test
	public void reset() {
		gate.reset(true);
		assertEquals(true, gate.openableModel().get().isOpen());

		gate.reset(false);
		assertEquals(false, gate.openableModel().get().isOpen());
	}

	@Test
	public void call() {
		gate.call();
		assertEquals(true, gate.openableModel().get().isOpen());
	}

	@Test
	public void callClose() {
		gate.call();
		gate.call();
		assertEquals(false, gate.openableModel().get().isOpen());
	}
}
