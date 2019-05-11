package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Openable.Lock;
import org.sarge.textrpg.common.Openable.OpenableException;
import org.sarge.textrpg.common.Openable.Operation;
import org.sarge.textrpg.common.Openable.State;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class OpenableTest {
	private Openable.Model model;
	private Lock lock;
	private Trap trap;

	@BeforeEach
	public void before() {
		trap = new Trap(Effect.NONE, Percentile.HALF);
		lock = new Lock(ObjectDescriptor.of("key"), Percentile.HALF, trap);
		model = new Openable.Model(lock);
	}

	/**
	 * Applies an openable operation that is expected to throw an action exception.
	 * @param op			Operation
	 * @param expected		Expected message
	 */
	private void apply(Operation op, String expected) {
		TestHelper.expect(OpenableException.class, expected, () -> model.apply(op));
	}

	@Test
	public void constructor() {
		assertNotNull(model);
		assertEquals(State.LOCKED, model.state());
		assertNotNull(model.holder());
		assertEquals(true, model.isLockable());
		assertEquals(lock, model.lock());
		assertEquals(false, model.isOpen());
		assertEquals(true, model.isTrapped());
	}

	@Test
	public void lock() {
		assertEquals(ObjectDescriptor.of("key"), lock.key());
		assertEquals(Percentile.HALF, lock.difficulty());
	}

	@Test
	public void latch() {
		model = new Openable.Model(Lock.LATCH);
		assertEquals(State.LOCKED, model.state());
		assertEquals(true, model.isLockable());
		assertEquals(Lock.LATCH, model.lock());
	}

	@Test
	public void fixed() {
		model = new Openable.Model(Lock.FIXED);
		assertEquals(State.CLOSED, model.state());
		assertEquals(false, model.isLockable());
		assertEquals(Lock.FIXED, model.lock());
		apply(Operation.OPEN, "openable.fixed.open");
	}

	@Test
	public void apply() throws ActionException {
		// Unlock
		model.apply(Operation.UNLOCK);
		assertEquals(State.CLOSED, model.state());
		assertEquals(false, model.isOpen());

		// Open
		model.apply(Operation.OPEN);
		assertEquals(State.OPEN, model.state());
		assertEquals(true, model.isOpen());

		// Close
		model.apply(Operation.CLOSE);
		assertEquals(State.CLOSED, model.state());
		assertEquals(false, model.isOpen());

		// Lock
		model.apply(Operation.LOCK);
		assertEquals(State.LOCKED, model.state());
		assertEquals(false, model.isOpen());
	}

	@Test
	public void duplicateState() throws ActionException {
		// Locked
		apply(Operation.LOCK, "openable.already.locked");

		// Unlocked
		model.apply(Operation.UNLOCK);
		apply(Operation.CLOSE, "openable.already.closed");

		// Opened
		model.apply(Operation.OPEN);
		apply(Operation.OPEN, "openable.already.open");
	}

	@Test
	public void invalidState() throws ActionException {
		// Locked
		apply(Operation.OPEN, "openable.state.locked");
		apply(Operation.CLOSE, "openable.state.locked");

		// Unlocked
		model.apply(Operation.UNLOCK);
		apply(Operation.CLOSE, "openable.already.closed");

		// Opened
		model.apply(Operation.OPEN);
		apply(Operation.OPEN, "openable.already.open");
		apply(Operation.LOCK, "openable.state.open");
	}

	@Test
	public void notLockable() {
		model = new Openable.Model(Lock.DEFAULT);
		assertEquals(false, model.isLockable());
		assertEquals(State.CLOSED, model.state());
		apply(Operation.LOCK, "openable.not.lockable");
	}

	@Test
	public void set() {
		model.set(State.OPEN);
		assertEquals(State.OPEN, model.state());
	}

	@Test
	public void setAlreadyState() {
		assertThrows(IllegalStateException.class, () -> model.set(State.LOCKED));
	}

	@Test
	public void setNotLockable() {
		model = new Openable.Model(Lock.DEFAULT);
		assertThrows(IllegalArgumentException.class, () -> model.set(State.LOCKED));
	}

	@Test
	public void reset() {
		model.set(State.OPEN);
		model.disarm();
		model.reset();
		assertEquals(State.LOCKED, model.state());
		assertEquals(false, model.isOpen());
		assertEquals(true, model.isTrapped());
	}

	@Test
	public void disarm() throws ActionException {
		model.disarm();
		assertEquals(false, model.isTrapped());
	}

	@Test
	public void disarmAlreadyDisarmed() throws ActionException {
		model.disarm();
		assertThrows(IllegalStateException.class, model::disarm);
	}

	@Test
	public void disarmNotTrapped() throws ActionException {
		model = new Openable.Model(Lock.DEFAULT);
		assertThrows(IllegalStateException.class, model::disarm);
	}

	@Test
	public void pick() {
		model.pick();
		assertEquals(State.CLOSED, model.state());
	}

	@Test
	public void pickNotLockable() {
		model = new Openable.Model(Lock.DEFAULT);
		assertThrows(IllegalStateException.class, model::pick);
	}

	@Test
	public void pickNotLocked() throws ActionException {
		model.apply(Operation.UNLOCK);
		assertThrows(IllegalStateException.class, model::pick);
	}

	@Test
	public void pickLatch() {
		model = new Openable.Model(Lock.LATCH);
		assertThrows(IllegalStateException.class, model::pick);
	}
}
