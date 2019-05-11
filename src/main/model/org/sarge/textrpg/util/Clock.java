package org.sarge.textrpg.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A <i>clock</i> is used to access the current game-time.
 * @author Sarge
 */
public interface Clock {
	/**
	 * System zone-id.
	 */
	ZoneId ZONE = ZoneId.of("UTC");

	/**
	 * Qualifier for the persisted start time.
	 */
	String START_TIME = "clock.start.time";

	/**
	 * @return Current time (epoch)
	 */
	long now();

	/**
	 * Adapter for a clock that converts to a date-time.
	 */
	interface DateTimeClock extends Clock {
		/**
		 * @return This clock as a date-time
		 */
		LocalDateTime toDateTime();

		/**
		 * Creates a date-time adapter for the given clock.
		 * @param clock Clock
		 * @return Date-time adapter
		 */
		static DateTimeClock of(Clock clock) {
			return new DateTimeClock() {
				@Override
				public long now() {
					return clock.now();
				}

				@Override
				public LocalDateTime toDateTime() {
					return LocalDateTime.ofInstant(Instant.ofEpochMilli(clock.now()), ZONE);
				}
			};
		}
	}
}
