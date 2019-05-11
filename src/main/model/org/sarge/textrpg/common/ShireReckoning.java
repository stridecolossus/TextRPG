package org.sarge.textrpg.common;

import java.time.LocalDate;

import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.MonthTable;
import org.springframework.stereotype.Component;

/**
 * Shire-reckoning calendar.
 * @see <a href="https://en.wikipedia.org/wiki/Middle-earth_calendar#Shire_calendar">Wikipedia</a>
 * @author Sarge
 */
@Component
public class ShireReckoning implements Calendar {
	private static final MonthTable MONTHS = new MonthTable.Builder()
		.add(1, "yule.2", MonthTable.Flag.SPECIAL)
		.add(30, "1")
		.add(30, "2")
		.add(30, "3")
		.add(30, "4")
		.add(30, "5")
		.add(30, "6")
		.add(1, "lithe.1", MonthTable.Flag.SPECIAL)
		.add(1, "midyear", MonthTable.Flag.SPECIAL)
		.add(1, "overlithe", MonthTable.Flag.SPECIAL, MonthTable.Flag.LEAPDAY)
		.add(1, "lithe.2", MonthTable.Flag.SPECIAL)
		.add(30, "7")
		.add(30, "8")
		.add(30, "9")
		.add(30, "10")
		.add(30, "11")
		.add(30, "12")
		.add(1, "yule.1", MonthTable.Flag.SPECIAL)
		.build();

	@Override
	public String name() {
		return "shire.reckoning";
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
		// Offset to start of SR calendar
		final LocalDate offset = date.plusDays(10);

		// Determine year
		final int year = date.getYear() - 1600;
		final boolean leap = isLeapYear(year);

		// Lookup month name
		final MonthTable.Month month = MONTHS.get(offset.getDayOfYear(), leap);

		// Determine week-day name
		if(month.isFlag(MonthTable.Flag.SPECIAL)) {
			return new Date(month.name(), year);
		}
		else {
			final int day = offset.getDayOfYear() - month.day();
			final int weekday = day % 7;
			return new Date(weekday + 1, day + 1, month.name(), year);
		}
	}
}
