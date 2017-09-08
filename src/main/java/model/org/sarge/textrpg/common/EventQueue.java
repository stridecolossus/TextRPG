package org.sarge.textrpg.common;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.object.EqualsBuilder;
import org.sarge.lib.object.ToString;

/**
 * Queue of pending events ordered by execution time.
 * @author Sarge
 */
public class EventQueue {
	private static final Collection<EventQueue> QUEUES = new StrictSet<>();

	/**
	 * Advances game time and executes pending events.
	 * @param time Time
	 */
	public static void update(long time) {
		for(EventQueue q : QUEUES) {
			q.execute(time);
		}
	}

	/**
	 * Queue entry.
	 */
	public class Entry implements Comparable<Entry> {
		private final Runnable event;
		private final long when;
		private final boolean repeating;
		private final long period;

		private boolean cancelled = false;

		/**
		 * Constructor.
		 * @param event			Event
		 * @param period		Scheduled time
		 * @param repeating		Whether this is a repeating event
		 */
		private Entry(Runnable event, long period, boolean repeating) {
			this.event = event;
			this.period = period;
			this.repeating = repeating;
			this.when = time + period;
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
			return (int) (this.when - that.when);
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

	private long time;

	/**
	 * Constructor.
	 */
	public EventQueue() {
		QUEUES.add(this);
	}

	/**
	 * @return Current time
	 */
	public long time() {
        return time;
    }

	/**
	 * Note that the stream is <b>not</b> ordered.
	 * @return Event queue
	 */
	Stream<EventQueue.Entry> stream() {
		return queue.stream();
	}

	/**
	 * @return Size of this queue
	 */
	public int size() {
	    return queue.size();
	}

	/**
	 * Registers an event to be executed after the given period.
	 * @param event			Event
	 * @param time			Scheduled time
	 * @param repeating		Whether this is a repeating event
	 */
	public synchronized Entry add(Runnable event, long time, boolean repeating) {
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
	public Entry add(Runnable event, long time) {
		return add(event, time, false);
	}

	/**
	 * Executes pending events up to the given time.
	 */
	public synchronized void execute(long time) {
	    // Advance time
	    this.time = time;

	    // Execute pending events
		while(!queue.isEmpty()) {
			// Stop when reach future events
			if(queue.peek().when > time) {
				break;
			}

			// Handle event
			final Entry entry = queue.poll();
			if(!entry.cancelled) {
				// Complete event
				entry.event.run();
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
	public synchronized void reset() {
		queue.clear();
	}

	@Override
	public String toString() {
		final ToString ts = new ToString(this);
		ts.append("size", queue.size());
		ts.append("time", time);
		return ts.toString();
	}
}
