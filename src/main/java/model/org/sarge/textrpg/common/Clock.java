package org.sarge.textrpg.common;

import java.util.HashSet;
import java.util.Set;

import org.sarge.lib.object.ToString;

/**
 * Game clock.
 * @author Sarge
 */
public class Clock {
	/**
	 * Listener on this clock.
	 */
	@FunctionalInterface
	public static interface Listener {
		/**
		 * Notifies a change of hour.
		 * @param hour Hour-of-day 0..23
		 */
		void update(int hour);
	}
	
	private final Set<Listener> listeners = new HashSet<>();
	
	private final long period;
	
	private int hour;

	/**
	 * Constructor.
	 * @param period Duration of an in-game hour (ms)
	 */
	public Clock(long period) {
		if(period < 1) throw new IllegalArgumentException("Period must be one-or-more");
		this.period = period;
	}

	/**
	 * Updates the current hour-of-day.
	 * @param time Current time
	 */
	public void update(long time) {
		// Calculate game-time hour-of-day
		final int next = (int) ((time % (period * 24)) / period);

		// Notify change of hour
		if(hour != next) {
			hour = next;
			listeners.forEach(listener -> listener.update(hour));
		}
	}
	
	/**
	 * @return Current hour-of-day 0..23
	 */
	public int getHour() {
		return hour;
	}
	
	/**
	 * @param hour Current hour
	 * @return Whether is day-light at the given hour
	 */
	public boolean isDaylight() {
		return (hour > 5) && (hour < 21);
	}
	
	/**
	 * Registers a listener on this clock.
	 * @param listener Listener
	 */
	public void add(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
