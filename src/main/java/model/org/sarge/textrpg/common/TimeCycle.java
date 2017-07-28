package org.sarge.textrpg.common;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.StreamUtil;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.util.Percentile;

/**
 * Day-night cycle.
 * @author Sarge
 */
public class TimeCycle implements Clock.Listener {
	/**
	 * Maps a table row to a period.
	 * <p>
	 * Table structure:
	 * <ul>
	 * <li><b>name</b> - period name</li>
	 * <li><b>hour</b> - hour-of-day</li>
	 * <li><b>vis</b> - light level</li>
	 * </ul>
	 * <p>
	 * @param table Data-table
	 */
	public static final Function<String[], Period> PERIOD_MAPPER = line -> {
		return new Period(
			line[0],
			line[3],
			Converter.INTEGER.convert(line[1]),
			Percentile.CONVERTER.convert(line[2])
		);
	};

	/**
	 * Period definition.
	 */
	public static final class Period {
		private final String key;
		private final String name;
		private final int hour;
		private final Percentile vis;

		/**
		 * Constructor.
		 * @param key		Period key
		 * @param name		Period name
		 * @param hour		Hour of day
		 * @param vis		Light level
		 */
		public Period(String key, String name, int hour, Percentile vis) {
			Check.notEmpty(key);
			Check.notEmpty(name);
			Check.range(hour, 0, 24);
			Check.notNull(vis);
			this.key = key;
			this.name = name;
			this.hour = hour;
			this.vis = vis;
		}

		public String getKey() {
			return key;
		}

		public String getName() {
			return name;
		}

		public int getHour() {
			return hour;
		}

		public Percentile getVisibility() {
			return vis;
		}

		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}

	/**
	 * Handler for period transitions.
	 */
	public interface Handler {
		/**
		 * Notifies the start of the next period.
		 * @param hour		Hour-of-day
		 * @param period	Period
		 */
		void update(int hour, Period period);
	}

	private final Map<Integer, Period> periods;

	private Period period;

	/**
	 * Constructor.
	 * @param periods		Periods
	 * @param hour			Current hour
	 */
	public TimeCycle(List<Period> periods, int hour) {
		Check.notEmpty(periods);
		Check.range(hour, 0, 24);
		this.periods = StreamUtil.toMap(periods, Period::getHour);
		this.period = periods.stream().filter(p -> p.hour >= hour).findFirst().orElse(periods.get(0));
	}

	/**
	 * @return Current period
	 */
	public Period getPeriod() {
		return period;
	}

	@Override
	public void update(int hour) {
		assert (hour >= 0) && (hour < 24);
		final Period p = periods.get(hour);
		if(p != null) {
			period = p;
		}
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
