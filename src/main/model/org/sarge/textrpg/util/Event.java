package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;
import java.util.AbstractQueue;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * An <i>event</i> is a call-back for something occurring in the future.
 * <p>
 * Events are registered on an {@link Event.Queue} which is managed by an {@link Event.Manager}.
 * <p>
 * An {@link Event.Holder} is used as a handle to a pending event.
 * <p>
 * Usage:
 * <pre>
 *   // Create a manager
 *   final Event.Manager manager = new Event.Manager();
 *
 *   // Create a queue
 *   final Event.Queue queue = manager.queue("name");
 *
 *   // Register an event
 *   final Event event = ...
 *   queue.add(event, when);
 *
 *   // Advance time and execute pending event
 *   manager.advance(...);
 * </pre>
 * @author Sarge
 */
@FunctionalInterface
public interface Event {
	/**
	 * Executes this event.
	 * @return Whether to repeat this event
	 */
	boolean execute();

	/**
	 * Empty (no-operation) event call-back.
	 */
	Event NONE = () -> false;

	/**
	 * Reference to an event registered with a queue.
	 */
	interface Reference {
		/**
		 * @return Whether the event has been cancelled (or executed)
		 */
		boolean isCancelled();

		/**
		 * Cancels this event.
		 */
		void cancel();
	}

	/**
	 * Queue of events.
	 */
	class Queue {
		private static final Logger LOG = LoggerFactory.getLogger(Event.Queue.class);

		/**
		 * Manager for a group of event queues.
		 */
		@Component
		public static class Manager implements Clock {
			private final Set<Queue> queues = Collections.newSetFromMap(new WeakHashMap<Queue, Boolean>());

			@Autowired //(required=false)
			@Qualifier(Clock.START_TIME)
			private long time;

			@Override
			public long now() {
				return time;
			}

			/**
			 * Creates a permanent event-queue managed by this manager.
			 * @param name Queue name
			 * @return New queue
			 */
			public synchronized Queue queue(String name) {
				return queue(name, false);
			}

			/**
			 * Creates an event-queue managed by this manager.
			 * @param name 		Queue name
			 * @param trans		Whether the queue is transient
			 * @return New queue
			 * @see Event.Queue#isTransient()
			 */
			public synchronized Queue queue(String name, boolean trans) {
				final Queue queue = new Queue(this, name, trans);
				queues.add(queue);
				return queue;
			}

			/**
			 * Advances time by the given increment.
			 * @param inc Time increment (ms)
			 */
			public void advance(long inc) {
				Check.oneOrMore(inc);
				time += inc;
				update();
			}

			/**
			 * Executes pending events.
			 */
			private void update() {
				for(Queue queue : queues) {
					queue.execute();
				}
			}

			/**
			 * Explicitly removes a queue managed by this manager.
			 * @param queue
			 * @throws IllegalArgumentException if the queue is not present
			 */
			private synchronized void remove(Queue queue) {
				if(!queues.contains(queue)) throw new IllegalArgumentException("Queue not present");
				queues.remove(queue);
			}
		}

		/**
		 * Event-queue entry.
		 */
		private static class Entry extends AbstractEqualsObject implements Comparable<Entry>, Reference {
			private final Event event;
			private final long duration;

			private long when;
			private volatile boolean cancelled;

			/**
			 * Constructor.
			 * @param event			Event call-back
			 * @param duration		Duration (ms)
			 */
			private Entry(Event event, long duration) {
				this.event = notNull(event);
				this.duration = oneOrMore(duration);
			}

			@Override
			public boolean isCancelled() {
				return cancelled;
			}

			@Override
			public void cancel() {
				assert !cancelled;
				cancelled = true;
			}

			/**
			 * (Re)schedules this event.
			 */
			private void schedule(long time) {
				assert time >= 0;
				this.when = time + duration;
			}

			@Override
			public int compareTo(Entry that) {
				return (int) (this.when - that.when);
			}
		}

		private final AbstractQueue<Entry> queue = new PriorityQueue<>();
		private final Manager manager;
		private final String name;
		private final boolean trans;

		/**
		 * Constructor.
		 * @param manager 	Manager for this queue
		 * @param name		Queue name
		 * @param trans		Whether this is a transient queue
		 */
		private Queue(Manager manager, String name, boolean trans) {
			this.manager = notNull(manager);
			this.name = notEmpty(name);
			this.trans = trans;
		}

		/**
		 * @return Manager of this queue
		 */
		public Manager manager() {
			return manager;
		}

		/**
		 * @return Name of this queue
		 */
		public String name() {
			return name;
		}

		/**
		 * @return Size of this queue
		 */
		public int size() {
			return queue.size();
		}

		/**
		 * @return Whether this is a transient queue that can be removed
		 * @see #Event(Manager, String, boolean)
		 */
		public boolean isTransient() {
			return trans;
		}

		/**
		 * Registers an event.
		 * @param entry 		Event call-back
		 * @param duration		Expiry duration
		 * @throws IllegalArgumentException if the duration is zero
		 */
		public synchronized Reference add(Event event, Duration duration) {
			DurationConverter.oneOrMore(duration);
			final Entry entry = new Entry(event, duration.toMillis());
			entry.schedule(manager.time);
			queue.add(entry);
			return entry;
		}

		/**
		 * Removes this queue.
		 * @throws IllegalStateException if this queue is not transient
		 * @see #isTransient()
		 */
		public synchronized void remove() {
			if(!trans) throw new IllegalStateException("Cannot remove a permanent queue: " + this);
			queue.clear();
			manager.remove(this);
		}

		/**
		 * Executes pending events up to the given time.
		 */
		private synchronized void execute() {
			while(!queue.isEmpty()) {
				// Stop when reach future events
				if(queue.peek().when > manager.time) {
					break;
				}

				// Skip cancelled events
				final Entry entry = queue.poll();
				if(entry.cancelled) {
					continue;
				}

				// Complete event
				final boolean repeat;
				try {
					repeat = entry.event.execute();
				}
				catch(Exception e) {
					LOG.error("Exception during event execution", e);
					entry.cancelled = true;
					continue;
				}

				if(repeat) {
					// Re-schedule repeating events
					entry.schedule(manager.time);
					queue.add(entry);
				}
				else {
					// Otherwise mark as executed
					entry.cancelled = true;
				}
			}
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("name", name)
				.append("size", size())
				.append("trans", trans)
				.append("manager", manager)
				.toString();
		}
	}

	/**
	 * An event <i>holder</i> contains a reference to a pending event.
	 * <p>
	 * Holders are used to manage events that may be cancelled or superseded in the future, e.g. a decay event for food which becomes redundant if the food is eaten or destroyed.
	 * <p>
	 * Usage:
	 * <pre>
	 *   // Create holder
	 *   final Event.Holder holder = new Event.Holder();
	 *
	 *   // Register an event
	 *   final Event.Reference ref = queue.add(...)
	 *   holder.set(ref);
	 *
	 *   // Event is superseded
	 *   final Event.Reference other = queue.add(...)
	 *   holder.set(other);
	 *
	 *   // Event is redundant
	 *   holder.cancel();
	 * </pre>
	 */
	class Holder extends AbstractEqualsObject {
		private Reference ref;

		/**
		 * Sets or replaces the referenced event.
		 * @param ref Event reference
		 * @throws IllegalArgumentException if the referenced event has already been cancelled
		 */
		public synchronized void set(Reference ref) {
			if(ref.isCancelled()) throw new IllegalArgumentException("Cannot hold an event that has already been cancelled");
			cancel();
			this.ref = notNull(ref);
		}

		/**
		 * Cancels the previous event if active.
		 */
		public synchronized void cancel() {
			if((ref != null) && !ref.isCancelled()) {
				ref.cancel();
				ref = null;
			}
		}
	}
}
