package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Openable.Lock;
import org.sarge.textrpg.common.Openable.Operation;
import org.sarge.textrpg.common.Openable.State;
import org.sarge.textrpg.util.Percentile;

public class OpenableTest extends ActionTest {
	private Openable model;
	private Lock lock;
	
	@Before
	public void before() {
		lock = new Lock("key", Percentile.ONE);
		model = new Openable(lock);
	}
	
	@Test
	public void constructor() {
		assertEquals(State.LOCKED, model.getState());
		assertEquals(false, model.isOpen());
		assertEquals(true, model.isLockable());
		assertEquals(lock, model.getLock());
		assertEquals("key", model.getLock().getKey());
		assertEquals(Percentile.ONE, model.getLock().getPickDifficulty());
		assertNotNull(model.getEventHolder());
	}
	
	@Test
	public void apply() throws ActionException {
		// Unlock
		model.apply(Operation.UNLOCK);
		assertEquals(State.CLOSED, model.getState());
		
		// Open
		model.apply(Operation.OPEN);
		assertEquals(State.OPEN, model.getState());
		
		// Close
		model.apply(Operation.CLOSE);
		assertEquals(State.CLOSED, model.getState());
		
		// Re-lock
		model.apply(Operation.LOCK);
		assertEquals(State.LOCKED, model.getState());
	}

	@Test
	public void applyAlready() throws ActionException {
		expect("openable.already");
		model.apply(Operation.LOCK);
	}
	
	@Test
	public void applyRequires() throws ActionException {
		expect("openable.invalid");
		model.apply(Operation.CLOSE);
	}

	@Test
	public void notLockable() throws ActionException {
		// Create and verify an un-lockable model
		model = new Openable();
		assertEquals(State.CLOSED, model.getState());
		assertEquals(Openable.UNLOCKABLE, model.getLock());
		assertEquals(false, model.isLockable());
		assertEquals(false, model.isOpen());
		
		// Check cannot be locked
		expect("openable.not.lockable");
		model.apply(Operation.LOCK);
	}
	
	@Test
	public void fixed() throws ActionException {
		model = new Openable(Openable.FIXED);
		assertEquals(State.CLOSED, model.getState());
		assertEquals(false, model.isLockable());
		assertEquals(false, model.isOpen());
		expect("openable.fixed");
		model.apply(Operation.OPEN);
	}
}