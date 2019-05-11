package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.DataTable;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.PeriodModel;

/**
 * A <i>time period</i> represents a part of the day, e.g. morning, dusk, etc.
 * @author Sarge
 */
public final class TimePeriod extends AbstractEqualsObject implements PeriodModel.Period {
	private final String name;
	private final LocalTime time;
	private final Percentile light;

	/**
	 * Constructor.
	 * @param name		Period name
	 * @param time		Start time
	 * @param light		Light-level during this period
	 */
	public TimePeriod(String name, LocalTime time, Percentile light) {
		this.name = notEmpty(name);
		this.time = notNull(time);
		this.light = notNull(light);
	}

	/**
	 * @return Period name
	 */
	public String name() {
		return name;
	}

	@Override
	public LocalTime start() {
		return time;
	}

	/**
	 * @return Ambient light-level during this period
	 */
	public Percentile light() {
		return light;
	}

	/**
	 * Loads a list of time-periods.
	 * <p>
	 * The format of a time-period is: <tt>name start-time light-level</tt>
	 * <br>
	 * where:
	 * <ul>
	 * <li><i>name</i> is the period description key</li>
	 * <li><i>start-time</i> is a local-time formatted string, e.g. <tt>23:45</tt></li>
	 * <li><i>light-level</i> is a percentile</li>
	 * <p>
	 * @param r Reader
	 * @return Time periods
	 * @throws IOException if the periods cannot be loaded
	 */
	public static List<TimePeriod> load(Reader r) throws IOException {
		final Function<String[], TimePeriod> loader = array -> {
			if(array.length != 3) throw new IllegalArgumentException("Invalid array length: " + Arrays.toString(array));
			final String name = array[0];
			final LocalTime start = LocalTime.parse(array[1]);
			final Percentile light = Percentile.CONVERTER.apply(array[2]);
			return new TimePeriod(name, start, light);
		};
		return DataTable.load(new BufferedReader(r)).map(loader).collect(toList());
	}
}
