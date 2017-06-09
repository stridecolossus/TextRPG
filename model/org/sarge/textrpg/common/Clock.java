package org.sarge.textrpg.common;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Game clock.
 * <p>
 * The <tt>hour.duration</tt> system property defines the real-time duration of a game-hour (ms), default is 5 minutes.
 * <p>
 * @author Sarge
 */
public final class Clock {
	/**
	 * Duration of a game-hour (ms).
	 */
	public static final long HOUR;
	
	// TODO - seems a bit kludgy?
	static {
		final String value = System.getProperties().getProperty("hour.duration");
		if(value == null) {
			HOUR = Duration.ofMinutes(5).toMillis();
		}
		else {
			HOUR = Long.parseLong(value);
			Check.oneOrMore(HOUR);
		}
	}
	
	/**
	 * Singleton clock.
	 */
	public static final Clock CLOCK = new Clock();
	
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
	
	private LocalDateTime datetime = LocalDateTime.now();

	/**
	 * Constructor.
	 */
	private Clock() {
		final Timer timer = new Timer(true);
		final TimerTask task = new TimerTask() {
			@Override
			public void run() {
				next();
			}
		};
		timer.schedule(task, HOUR, HOUR);
	}

	/**
	 * Sets the current world time.
	 * @param datetime World-time
	 */
	public void setDateTime(LocalDateTime datetime) {
		Check.notNull(datetime);
		this.datetime = datetime;
	}
	
	/**
	 * @return Current world-time
	 */
	public LocalDateTime getWorldTime() {
		return datetime;
	}
	
	/**
	 * @return Current hour-of-day 0..23
	 */
	public int getHour() {
		return datetime.getHour();
	}
	
	/**
	 * Registers a listener on this clock.
	 * @param listener Listener
	 */
	public void add(Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a listener on this clock.
	 * @param listener Listener to remove
	 */
	public void remove(Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * Move to next hour.
	 */
	public void next() {
		// Advance time
		datetime = datetime.plusHours(1);
		
		// Notify listeners
		final int hour = datetime.getHour();
		listeners.forEach(listener -> listener.update(hour));
	}
	
	/**
	 * Resets this clock.
	 */
	public void reset() {
		listeners.clear();
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
