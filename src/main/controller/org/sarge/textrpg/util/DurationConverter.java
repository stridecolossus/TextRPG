package org.sarge.textrpg.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import org.sarge.lib.util.Converter;

/**
 * Duration converter.
 * <p>
 * Supports both standard ISO and a custom format.
 * <p>
 * The custom format consists of a <i>number</i> followed by one of the following <i>time-units</i>:
 * <ul>
 * <li><tt>h</tt> - hours</li>
 * <li><tt>m</tt> - minutes</li>
 * <li><tt>s</tt> - seconds</li>
 * <li><tt>ms</tt> - milliseconds</li>
 * </ul>
 * <p>
 * Examples:
 * <ul>
 * <li><tt>1h</tt> - 1 hour</li>
 * <li><tt>25m</tt> - 25 minutes</li>
 * <li><tt>90s</tt> - 90 seconds</li>
 * <li><tt>500ms</tt> - 500 milliseconds</li>
 * </ul>
 * @see Duration#parse(CharSequence)
 * @author Sarge
 */
public class DurationConverter implements Converter<Duration> {
	/**
	 * Duration converter singleton instance.
	 */
	public static final DurationConverter CONVERTER = new DurationConverter();

	/**
	 * Verifies that the given duration is one-or-more.
	 * @param duration Duration
	 * @return Duration
	 * @throws IllegalArgumentException if the given duration is zero or negative
	 */
	public static Duration oneOrMore(Duration duration) {
		if(duration.isZero() || duration.isNegative()) throw new IllegalArgumentException("Duration must be one-or-more");
		return duration;
	}

	private DurationConverter() {
		// Singleton
	}

	@Override
	public Duration apply(String str) {
		if(str.startsWith("P")) {
			// Parse ISO formatted duration
			return Duration.parse(str);
		}
		else {
			// Parse custom format
			final TemporalUnit unit = unit(str);
			final int suffix = unit == ChronoUnit.MILLIS ? 2 : 1;
			final String sub = str.substring(0, str.length() - suffix);
			final int num = Converter.INTEGER.apply(sub);
			if(num <= 0) throw new NumberFormatException("Number of units must be one-or-more: " + str);
			return Duration.of(num, unit);
		}
	}

	/**
	 * Determines the time-unit of the given duration.
	 * @param str String
	 * @return Time-unit
	 */
	private static TemporalUnit unit(String str) {
		final char ch = str.charAt(str.length() - 1);
		switch(ch) {
		case 'd':
			return ChronoUnit.DAYS;

		case 'h':
			return ChronoUnit.HOURS;

		case 'm':
			return ChronoUnit.MINUTES;

		case 's':
			if(str.endsWith("ms")) {
				return ChronoUnit.MILLIS;
			}
			else {
				return ChronoUnit.SECONDS;
			}

		default:
			throw new NumberFormatException("Invalid time-unit suffix: " + str);
		}
	}
}
