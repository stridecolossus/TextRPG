package org.sarge.textrpg.common;

import java.time.LocalDate;

import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.MonthTable;

/**
 * Calendar of Imladris.
 * @see <a href="https://en.wikipedia.org/wiki/Middle-earth_calendar#Shire_calendar">Wikipedia</a>
 * @author Sarge
 */
public class ImladrisCalendar implements Calendar {
	private static final MonthTable MONTHS = new MonthTable.Builder()
		.add(54, "tuile")
		.add(72, "tuile")
		.add(54, "tuile")
		.add(3, "other.1")
		.add(54, "tuile")
		.add(72, "tuile")
		.add(54, "tuile")
		.add(2, "other.2")
		.add(1, "leapday", MonthTable.Flag.LEAPDAY)
		.build();

	@Override
	public String name() {
		return "calendar.imladris";
	}

	@Override
	public boolean isLeapYear(int year) {
		if((year % 4) == 0) {
			return (year % 100) != 0;
		}
		else {
			return false;
		}
	}

	@Override
	public Date date(LocalDate date) {

		/**
		 * - 6 day week
		 * - leap year calculation?
		 *
		 */

		// Offset to start of SR calendar
		final LocalDate offset = date.plusDays(10);

		// Determine year
		final int year = date.getYear() - 1600;
		final boolean leap = isLeapYear(year);

		// Lookup month name
		final MonthTable.Month month = MONTHS.get(offset.getDayOfYear(), leap);

		// Determine week-day name
		final String day;
		if(month.isFlag(MonthTable.Flag.SPECIAL)) {
			day = month.name();
		}
		else {
			final int index = (offset.getDayOfYear() - (month.day() + 1)) % 7;
			day = String.valueOf(index + 1);
		}

		// Create day descriptor
		// TODO
		return new Date(1, 2, month.name(), year);
	}
}
