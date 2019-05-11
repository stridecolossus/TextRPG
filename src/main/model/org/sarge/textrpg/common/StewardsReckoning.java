package org.sarge.textrpg.common;

import java.time.LocalDate;

import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.MonthTable;
import org.springframework.stereotype.Component;

/**
 * Stewards-reckoning calendar.
 * @see <a href="https://en.wikipedia.org/wiki/Middle-earth_calendar#Shire_calendar">Wikipedia</a>
 * @author Sarge
 */
@Component
public class StewardsReckoning implements Calendar {
	private static final MonthTable MONTHS = new MonthTable.Builder()
		.add(1, "midwinter.1", MonthTable.Flag.SPECIAL)
		.add(30, "1")
		.add(30, "2")
		.add(30, "3")
		.add(1, "equinox.1", MonthTable.Flag.SPECIAL)
		.add(30, "4")
		.add(30, "5")
		.add(30, "6")
		.add(1, "midyear", MonthTable.Flag.SPECIAL)
		.add(1, "leapday", MonthTable.Flag.SPECIAL, MonthTable.Flag.LEAPDAY)
		.add(30, "7")
		.add(30, "8")
		.add(30, "9")
		.add(1, "equinox.2", MonthTable.Flag.SPECIAL)
		.add(30, "10")
		.add(30, "11")
		.add(30, "12")
		.add(1, "midwinter.2", MonthTable.Flag.SPECIAL)
		.build();

	@Override
	public String name() {
		return "stewards.reckoning";
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

		// TODO
		// - move offset to month-table (always 10?)
		// - add week length to month-table
		// - move logic to month-table => month-table extends Calendar, this is an instance?
		// - remove leap-year calculation (conflicts with LocalDate)
		// - mapper for year (-1600 for shire, ~ TA1 for others)

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
