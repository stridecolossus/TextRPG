package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.After;
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
	
	@After
	public void after() {
		Gate.QUEUE.reset();
	}
	
	@Test
	public void constructor() {
		assertNotNull(gate.getOpenableModel());
		assertEquals(true, gate.getOpenableModel().isPresent());
		assertEquals(false, gate.getOpenableModel().get().isOpen());
		assertEquals(true, gate.isFixture());
	}
	
	@Test
	public void reset() {
		gate.reset(true);
		assertEquals(true, gate.getOpenableModel().get().isOpen());
		
		gate.reset(false);
		assertEquals(false, gate.getOpenableModel().get().isOpen());
	}
	
	@Test
	public void call() {
		gate.call();
		assertEquals(true, gate.getOpenableModel().get().isOpen());
	}
	
	@Test
	public void callClose() {
		gate.call();
		gate.call();
		assertEquals(false, gate.getOpenableModel().get().isOpen());
	}
}
