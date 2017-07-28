package org.sarge.textrpg.common;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;

import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.StrictSet;
import org.sarge.lib.util.ToString;

/**
 * Queue of pending events ordered by execution time.
 * @author Sarge
 */
public class EventQueue {
	private static final Collection<EventQueue> QUEUES = new StrictSet<>();

	private static long time;

	/**
	 * @return Current game time
	 */
	public static long getTime() {
		return time;
	}

	/**
	 * Advances game time and executes pending events.
	 * @param time Time
	 */
	public static void update(long time) {
		// Advance time
		if(time <= EventQueue.time) throw new RuntimeException("Invalid time update");
		EventQueue.time = time;

		// Execute pending events
		for(final EventQueue q : QUEUES) {
			q.execute(time);
		}
	}

	/**
	 * Queue entry.
	 */
	public class Entry implements Comparable<Entry> {
		private final Event event;
		private final long time;
		private final boolean repeating;
		private final long period;

		private boolean cancelled = false;

		/**
		 * Constructor.
		 * @param event			Event
		 * @param period		Scheduled time
		 * @param repeating		Whether this is a repeating event
		 */
		private Entry(Event event, long period, boolean repeating) {
			this.event = event;
			this.period = period;
			this.repeating = repeating;
			this.time = EventQueue.time + period;
		}

		/**
		 * @return Scheduled execution time
		 */
		public long getTime() {
			return time;
		}

		/**
		 * @return Whether this event has been cancelled.
		 */
		public boolean isCancelled() {
			return cancelled;
		}

		/**
		 * Cancels this event.
		 */
		public void cancel() {
			assert !cancelled;
			cancelled = true;
		}

		@Override
		public int compareTo(Entry that) {
			return (int) (this.time - that.time);
		}

		@Override
		public boolean equals(Object that) {
			return EqualsBuilder.equals(this, that);
		}

		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}

	private final Queue<Entry> queue = new PriorityQueue<>();

	/**
	 * Constructor.
	 */
	public EventQueue() {
		QUEUES.add(this);
	}

	/**
	 * Note that the stream is <b>not</b> ordered.
	 * @return Event queue
	 */
	public Stream<EventQueue.Entry> stream() {
		return queue.stream();
	}

	/**
	 * Registers an event to be executed after the given period.
	 * @param event			Event
	 * @param time			Scheduled time
	 * @param repeating		Whether this is a repeating event
	 */
	public Entry add(Event event, long time, boolean repeating) {
		final Entry entry = new Entry(event, time, repeating);
		queue.add(entry);
		return entry;
	}

	/**
	 * Registers a non-repeating event.
	 * @param event		Event
	 * @param time		Scheduled time
	 * @return
	 */
	public Entry add(Event event, long time) {
		return add(event, time, false);
	}

	/**
	 * Executes pending events up to the given time.
	 */
	public synchronized void execute(long time) {
		while(!queue.isEmpty()) {
			// Stop when reach future events
			if(queue.peek().time > time) {
				break;
			}

			// Handle event
			final Entry entry = queue.poll();
			if(!entry.cancelled) {
				// Complete event
				entry.event.execute();
				entry.cancelled = true;

				// Re-schedule if repeating
				if(entry.repeating) {
					add(entry.event, entry.period, true);
				}
			}
		}
	}

	/**
	 * Resets this queue.
	 */
	public void reset() {
		queue.clear();
	}

	@Override
	public String toString() {
		final ToString ts = new ToString(this);
		ts.append("size", queue.size());
		ts.append("time", EventQueue.time);
		return ts.toString();
	}
}
