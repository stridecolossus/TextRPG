package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextHelper;

/**
 * Defines something that can be opened and optionally locked.
 * <p>
 * The {@link Model} for an openable has a {@link Lock} that specifies the key for the lock, whether it can be picked and its optional {@link Trap} descriptor.
 * <p>
 * Pre-defined lock descriptors are provided for simpler openable objects:
 * <ul>
 * <li>The {@link Lock#DEFAULT} lock defines an object that cannot be locked (only opened or closed)</li>
 * <li>A {@link Lock#LATCH} can be locked without a key</li>
 * </ul>
 * Notes:
 * <ul>
 * <li>The {@link Model#state()} of an openable can be changed using the {@link Model#apply(Operation)} method</li>
 * <li>{@link Model#set(State)} is used to override the standard operations, e.g. for scripted events</li>
 * <li>A lock can be picked using {@link Model#pick()}</li>
 * <li>A trapped openable can be disarmed via {@link Model#disarm()}</li>
 * <li>{@link Model#reset()} is invoked when an openable is reset to its initial state and should be over-ridden in sub-classes</li>
 * </ul>
 * @author Sarge
 */
@FunctionalInterface
public interface Openable {
	/**
	 * @return Model for this openable
	 */
	Model model();

	/**
	 * State of this openable.
	 */
	enum State {
		OPEN,
		CLOSED,
		LOCKED
	}

	/**
	 * Exception for an invalid operation on this openable.
	 */
	class OpenableException extends RuntimeException {
		/**
		 * Constructor.
		 * @param reason Reason code
		 */
		public OpenableException(String reason) {
			super(reason);
		}

		/**
		 * Constructor.
		 * @param reason		Reason code
		 * @param state			Openable state argument
		 */
		private OpenableException(String reason, State state) {
			this(TextHelper.join(reason, state));
		}
	}

	/**
	 * Operations that can be performed on this openable.
	 */
	enum Operation {
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
		 * @return Whether this operation can only be applied to a lockable model
		 * @see Openable#isLockable()
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
	 * Descriptor for a lock.
	 */
	final class Lock extends AbstractEqualsObject {
		/**
		 * Lock for an openable that cannot be locked.
		 */
		public static final Lock DEFAULT = new Lock();

		/**
		 * A latch can be locked but does not require a key.
		 */
		public static final Lock LATCH = new Lock();

		/**
		 * Lock for an openable that cannot be manipulated by an actor, e.g. a sealed portal.
		 */
		public static final Lock FIXED = new Lock();

		private final ObjectDescriptor key;
		private final Percentile pick;
		private final Optional<Trap> trap;

		/**
		 * Constructor.
		 * @param key		Key to this lock
		 * @param pick		Lock-pick difficulty
		 * @param trap		Optional trap
		 */
		public Lock(ObjectDescriptor key, Percentile pick, Trap trap) {
			this.key = notNull(key);
			this.pick = notNull(pick);
			this.trap = Optional.ofNullable(trap);
		}

		/**
		 * Default constructor for an openable that cannot be locked.
		 */
		private Lock() {
			this.key = null;
			this.pick = null;
			this.trap = Optional.empty();
		}

		/**
		 * @return Descriptor for the key to this lock
		 */
		public ObjectDescriptor key() {
			return key;
		}

		/**
		 * @return Lock-pick difficulty
		 */
		public Percentile difficulty() {
			return pick;
		}

		/**
		 * @return Trap model
		 */
		public Optional<Trap> trap() {
			return trap;
		}
	}

	/**
	 * Openable model.
	 */
	class Model extends AbstractEqualsObject {
		private final Lock lock;
		private final Event.Holder holder = new Event.Holder();

		private State state;
		private boolean trapped;

		/**
		 * Constructor.
		 * @param lock Lock descriptor
		 */
		public Model(Lock lock) {
			this.lock = notNull(lock);
			init();
		}

		/**
		 * @return Lock descriptor
		 */
		public Lock lock() {
			return lock;
		}

		/**
		 * @return Current state
		 */
		public State state() {
			return state;
		}

		/**
		 * @return Whether this model is open
		 */
		public boolean isOpen() {
			return state == State.OPEN;
		}

		/**
		 * @return Whether this openable can be locked
		 */
		public boolean isLockable() {
			if(lock == Lock.LATCH) {
				return true;
			}
			else {
				return lock.key != null;
			}
		}

		/**
		 * @return Holder for reset event for this openable
		 */
		public Event.Holder holder() {
			return holder;
		}

		/**
		 * Applies the given operation to this openable.
		 * @param op Operation to perform
		 * @throws OpenableException if the given operation cannot be performed
		 */
		public void apply(Operation op) throws OpenableException {
			if(state == op.after) throw new OpenableException("openable.already", op.after);
			if(state != op.before) throw new OpenableException("openable.state", state);
			if(lock == Lock.FIXED) throw new OpenableException("openable.fixed", op.after);
			if(op.isLocking() && !isLockable()) throw new OpenableException("openable.not.lockable");
			update(op.after);
		}

		/**
		 * Forces the state of this openable.
		 * @param state State
		 * @throws IllegalArgumentException if the state is {@link State#LOCKED} but this openable cannot be locked
		 * @throws IllegalStateException if this openable is already in the given state
		 */
		public void set(State state) {
			if((state == State.LOCKED) && !isLockable()) throw new IllegalArgumentException("Cannot lock an unlockable openable: " + this);
			if(state == this.state) throw new IllegalStateException(String.format("Openable state would not be changed: openable=%s state=%s", this, state));
			update(state);
		}

		/**
		 * @return Whether the lock of this openable is trapped
		 */
		public boolean isTrapped() {
			return trapped;
		}

		/**
		 * Disarms the trap on the lock of this openable.
		 * @throws IllegalStateException if this openable is not trapped
		 */
		public void disarm() {
			if(!trapped) throw new IllegalStateException("Not trapped");
			trapped = false;
		}

		/**
		 * Picks the lock of this openable.
		 * @throws IllegalStateException if this model is not locked or is a {@link Lock#LATCH}
		 */
		public void pick() {
			if(!isLockable() || (state != State.LOCKED) || (lock == Lock.LATCH)) throw new IllegalStateException("Cannot pick: " + this);
			state = State.CLOSED;
			trapped = false;
		}

		/**
		 * @return Default state of this openable
		 */
		private State defaultState() {
			if(isLockable()) {
				return State.LOCKED;
			}
			else {
				return State.CLOSED;
			}
		}

		/**
		 * Initialises this openable.
		 */
		private void init() {
			state = defaultState();
			trapped = lock.trap.isPresent();
		}

		/**
		 * Updates the state of this openable.
		 * @param state New state
		 */
		private void update(State state) {
			// Update state
			this.state = state;

			// Update reset event
			if(state == defaultState()) {
				holder.cancel();
			}
		}

		/**
		 * Resets this openable to its initial state.
		 */
		public void reset() {
			init();
			holder.cancel();
		}
	}
}
