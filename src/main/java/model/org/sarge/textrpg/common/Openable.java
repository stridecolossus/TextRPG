package org.sarge.textrpg.common;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.util.Percentile;

/**
 * Model for something that can opened and optionally locked.
 * @author Sarge
 */
public class Openable {
	/**
	 * States of an openable object.
	 */
	public static enum State {
		OPEN,
		CLOSED,
		LOCKED
	}

	/**
	 * Operations that can be applied to this openable.
	 */
	public enum Operation {
		OPEN(State.CLOSED, State.OPEN),
		CLOSE(State.OPEN, State.CLOSED),
		LOCK(State.CLOSED, State.LOCKED),
		UNLOCK(State.LOCKED, State.CLOSED);

		private final State before, after;

		private Operation(State before, State after) {
			this.before = before;
			this.after = after;
		}

		/**
		 * @return Openable state after applying this operation
		 */
		public State getState() {
			return after;
		}

		/**
		 * @return Whether this is an operation for a lockable object
		 */
		public boolean isLocking() {
			switch(this) {
			case LOCK:
			case UNLOCK:
				return true;

			default:
				return false;
			}
		}
	}

	/**
	 * Descriptor for a lockable object.
	 */
	public static class Lock {
		private final String key;
		private final Percentile pick;

		/**
		 * Constructor.
		 * @param key		Key name
		 * @param pick		Pick difficulty
		 */
		public Lock(String key, Percentile pick) {
			Check.notNull(key);
			Check.notNull(pick);
			this.key = key;
			this.pick = pick;
		}

		private Lock() {
			this.key = null;
			this.pick = null;
		}

		public String getKey() {
			return key;
		}

		public Percentile getPickDifficulty() {
			return pick;
		}

		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}

	/**
	 * Lock descriptor for an object that cannot be locked.
	 */
	public static final Lock UNLOCKABLE = new Lock();

	/**
	 * Lock descriptor for an object that cannot be manually opened.
	 */
	public static final Lock FIXED = new Lock();

	private final Lock lock;
	private final Event.Holder reset = new Event.Holder();

	private State state;

	/**
	 * Constructor.
	 * @param lock Optional lock descriptor
	 */
	public Openable(Lock lock) {
		Check.notNull(lock);
		this.lock = lock;
		reset();
	}

	/**
	 * Default constructor for an openable that cannot be locked.
	 */
	public Openable() {
		this(UNLOCKABLE);
	}

	/**
	 * @return Lock descriptor
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * @return Whether this object is open
	 */
	public boolean isOpen() {
		return state == State.OPEN;
	}

	/**
	 * @return Whether this openable object can be locked
	 */
	public boolean isLockable() {
		return lock.key != null;
	}

	/**
	 * @return State of this openable
	 */
	public State getState() {
		return state;
	}

	/**
	 * @return Holder for the current reset event
	 */
	public Event.Holder getResetEventHolder() {
		return reset;
	}

	/**
	 * Applies the given operation to this openable.
	 * @param op Operation to apply
	 * @throws ActionException if the operation cannot be applied to this openable
	 */
	public void apply(Operation op) throws ActionException {
		if(lock == FIXED) throw new ActionException("openable.fixed", op);
		if(op.isLocking() && !isLockable()) throw new ActionException("openable.not.lockable", op);
		if(state == op.after) throw new ActionException("openable.already", op.after);
		if(state != op.before) throw new ActionException("openable.invalid." + op);
		this.state = op.after;
	}

	/**
	 * Open/closes this openable.
	 */
	public void setOpen(boolean open) {
		this.state = open ? State.OPEN : State.CLOSED;
	}

	/**
	 * Resets this openable to its initial state.
	 */
	public void reset() {
		this.state = isLockable() ? State.LOCKED : State.CLOSED;
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
