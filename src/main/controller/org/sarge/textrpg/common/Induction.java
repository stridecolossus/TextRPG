package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Event;

/**
 * An induction is a delayed action performed by an actor, e.g. casting a spell.
 * @author Sarge
 */
@FunctionalInterface
public interface Induction {
	/**
	 * Induction flags.
	 */
	enum Flag {
		/**
		 * Displays spinner during this induction, e.g. search.
		 */
		SPINNER,

		/**
		 * Induction is repeating, e.g. whet weapon.
		 */
		REPEATING,

		/**
		 * Induction is a <i>primary</i> induction, e.g. combat or swimming.
		 */
		PRIMARY
	}

	/**
	 * Completes an iteration of this induction.
	 * @return Response
	 * @throws ActionException if this induction cannot be completed
	 */
	Response complete() throws ActionException;

	/**
	 * Interrupts this induction (default implementation does nothing).
	 */
	default void interrupt() {
		// Does nothing
	}

	/**
	 * Descriptor for an induction.
	 */
	final class Descriptor extends AbstractEqualsObject {
		private final Supplier<Duration> period;
		private final Set<Flag> flags;

		/**
		 * Constructor.
		 * @param period	Iteration period or {@link Duration#ZERO} for an indefinite induction
		 * @param flags		Induction flags
		 */
		private Descriptor(Supplier<Duration> period, EnumSet<Flag> flags) {
			this.period = notNull(period);
			this.flags = EnumSet.copyOf(flags);
		}

		/**
		 * @param flag Induction flag
		 * @return Whether this induction supports the given flag
		 */
		public boolean isFlag(Flag flag) {
			return flags.contains(flag);
		}

		/**
		 * Builder for an induction descriptor.
		 */
		public static class Builder {
			private Supplier<Duration> period = () -> Duration.ZERO;
			private final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);

			/**
			 * Sets the induction iteration period.
			 * @param period Iteration period
			 */
			public Builder period(Supplier<Duration> period) {
				this.period = period;
				return this;
			}

			/**
			 * Convenience method to set a fixed induction period.
			 * @param period Iteration period
			 */
			public Builder period(Duration period) {
				this.period = () -> period;
				return this;
			}

			/**
			 * Toggles an induction flag.
			 * @param f Flag to toggle
			 */
			public Builder flag(Flag f) {
				if(flags.contains(f)) {
					flags.remove(f);
				}
				else {
					flags.add(f);
				}
				return this;
			}

			/**
			 * @return New induction descriptor
			 */
			public Descriptor build() {
				return new Descriptor(period, flags);
			}
		}
	}

	/**
	 * Induction instance.
	 */
	final class Instance extends AbstractEqualsObject {
		/**
		 * Creates an indefinite induction.
		 * @param stop Stop callback
		 * @return Indefinite induction
		 */
		public static Instance indefinite(Runnable stop) {
			final Induction induction = new Induction() {
				@Override
				public Response complete() throws ActionException {
					throw new UnsupportedOperationException();
				}

				@Override
				public void interrupt() {
					stop.run();
				}
			};
			final Descriptor descriptor = new Descriptor.Builder().build();
			return new Instance(descriptor, induction);
		}

		private final Event.Holder holder = new Event.Holder();
		private final Descriptor descriptor;
		private final Induction induction;

		/**
		 * Constructor.
		 * @param descriptor		Induction descriptor
		 * @param induction			Induction
		 */
		public Instance(Descriptor descriptor, Induction induction) {
			this.descriptor = notNull(descriptor);
			this.induction = notNull(induction);
		}

		/**
		 * Convenience constructor for a default induction.
		 * @param induction			Induction callback
		 * @param duration			Iteration period
		 */
		public Instance(Induction induction, Duration period) {
			this(build(period), induction);
		}

		/**
		 * Constructs a default induction descriptor.
		 * @param period Induction period
		 * @return Default induction descriptor
		 */
		private static Induction.Descriptor build(Duration period) {
			return new Descriptor.Builder().period(period).flag(Flag.SPINNER).build();
		}

		/**
		 * @return Induction descriptor
		 */
		public Descriptor descriptor() {
			return descriptor;
		}

		/**
		 * @return Induction
		 */
		public Induction induction() {
			return induction;
		}
	}

	/**
	 * Manager for active inductions.
	 * <p>
	 * The manager differentiates between an <i>active</i> induction and the <i>primary</i> induction.
	 * An active induction is some action started by an actor such as picking a lock.
	 * The primary induction is an entity state that may be initiated by another entity or the environment such as combat and swimming.
	 * <p>
	 * In either case only <b>one</b> of each type of induction can be active at any given time.
	 * The major difference between the two is that the primary induction can be started even if an active induction is already running (and may cause the actor to lose concentration).
	 * <p>
	 * Usage:
	 * <pre>
	 * // Create active induction
	 * final Instance instance = new Instance(descriptor, induction);
	 *
	 * // Start active induction
	 * final Manager manager = ...
	 * manager.start(induction);
	 *
	 * // Start combat
	 * manager.start(combat);
	 *
	 * // Interrupt active induction
	 * manager.interrupt();
	 *
	 * // Stop combat
	 * manager.stop();
	 * </pre>
	 */
	class Manager extends AbstractObject {
		private final Event.Queue queue;
		private final Consumer<Response> listener;

		private Instance primary;
		private Instance active;

		/**
		 * Constructor.
		 * @param queue 		Induction queue
		 * @param listener		Listener for iteration responses and errors
		 */
		public Manager(Event.Queue queue, Consumer<Response> listener) {
			this.queue = notNull(queue);
			this.listener = notNull(listener);
		}

		/**
		 * @return Response listener
		 */
		public Consumer<Response> listener() {
			return listener;
		}

		/**
		 * @return Whether an <i>active</i> induction is active
		 */
		public boolean isActive() {
			return active != null;
		}

		/**
		 * @return Whether a <i>primary</i> induction is active
		 */
		public boolean isPrimary() {
			return primary != null;
		}

		/**
		 * Starts an induction.
		 * @param instance Induction instance
		 * @throws IllegalStateException if an induction of the given type is already active
		 */
		public void start(Instance instance) {
			// Add induction
			final Descriptor descriptor = instance.descriptor();
			if(descriptor.isFlag(Flag.PRIMARY)) {
				if(primary != null) throw new IllegalStateException("Primary Induction already active: " + primary);
				this.primary = instance;
			}
			else {
				if(isActive()) throw new IllegalStateException("Induction already active: " + active);
				this.active = instance;
			}

			// Register induction event
			final Duration duration = descriptor.period.get();
			if(duration != Duration.ZERO) {
				final Event.Reference ref = queue.add(() -> update(instance), duration);
				instance.holder.set(ref);
			}
		}

		/**
		 * Interrupts the currently <i>active</i> induction.
		 * @throws IllegalStateException if there is no active induction
		 */
		public void interrupt() {
			if(!isActive()) throw new IllegalStateException("No active induction to interrupt");
			active.induction.interrupt();
			active.holder.cancel();
			active = null;
		}

		/**
		 * Stops the <i>primary</i> induction.
		 * @throws IllegalStateException if there is no primary induction
		 */
		public void stop() {
			if(primary == null) throw new IllegalStateException("No primary induction");
			primary.holder.cancel();
			primary = null;
		}

		/**
		 * Induction event callback.
		 * @param instance Induction instance
		 * @return Whether repeating
		 */
		private boolean update(Instance instance) {
			// Complete induction
			try {
				final Response response = instance.induction.complete();
				listener.accept(response);
			}
			catch(ActionException e) {
				listener.accept(Response.of(e.description()));
				if(!instance.descriptor.isFlag(Flag.PRIMARY)) {
					active = null;
				}
				return false;
			}

			// Stop finished inductions
			if(instance.descriptor.isFlag(Flag.REPEATING)) {
				return true;
			}
			else {
				active = null;
				return false;
			}
		}
	}
}
