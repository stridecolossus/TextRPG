package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;
import org.sarge.lib.util.Check;

/**
 * Table of month descriptors.
 * @author Sarge
 */
public class MonthTable extends AbstractObject {
	private static final int DAYS_IN_YEAR = 365;

	/**
	 * Month flags.
	 */
	public static enum Flag {
		/**
		 * This month does not have week-days.
		 */
		SPECIAL,

		/**
		 * This month is only present in a leap-year.
		 */
		LEAPDAY
	}

	/**
	 * Month descriptor.
	 */
	public static final class Month extends AbstractEqualsObject {
		private final String name;
		private final int len;
		private final Set<Flag> flags;
		private final int day;

		/**
		 * Constructor.
		 * @param name		Month name
		 * @param len		Length
		 * @param flags		Flags
		 * @param day		Day-of-year when this month starts
		 */
		private Month(String name, int len, EnumSet<Flag> flags, int day) {
			this.name = notEmpty(name);
			this.len = oneOrMore(len);
			this.flags = EnumSet.copyOf(flags);
			this.day = oneOrMore(day);
		}

		/**
		 * @return Month name
		 */
		public String name() {
			return name;
		}

		/**
		 * @return Number of days in this month
		 */
		public int length() {
			return len;
		}

		/**
		 * @param f Flag
		 * @return Whether this month has the given flag
		 */
		public boolean isFlag(Flag f) {
			return flags.contains(f);
		}

		/**
		 * @return Day-of-year when this month starts
		 */
		public int day() {
			return day;
		}
	}

	private final List<Month> months;

	/**
	 * Constructor.
	 * @param months Months
	 */
	private MonthTable(List<Month> months) {
		this.months = List.copyOf(months);
	}

	/**
	 * Looks up the month descriptor for the given day.
	 * @param dayOfYear 	Day-of-year 1..365/366
	 * @param leap			Whether this is a leap-year
	 * @return Month descriptor
	 */
	public Month get(int dayOfYear, boolean leap) {
		Check.range(dayOfYear, 1, DAYS_IN_YEAR + (leap ? 1 : 0));
		int end = 0;
		for(Month m : months) {
			// Ignore leap-days if not a leap-year
			if(!leap && m.isFlag(Flag.LEAPDAY)) {
				continue;
			}

			// Find month
			end += m.len;
			if(dayOfYear <= end) {
				return m;
			}
		}
		throw new RuntimeException();
	}

	/**
	 * Builder for a month-table.
	 */
	public static class Builder {
		private static final EnumSet<Flag> EMPTY = EnumSet.noneOf(Flag.class);

		private final List<Month> months = new ArrayList<>();
		private int day = 1;
		private boolean leap;

		/**
		 * Adds a month.
		 * @param length	Number of days in this month
		 * @param name		Month name
		 * @param flags		Flags
		 */
		public Builder add(int length, String name, Flag... flags) {
			// Build flag-set
			final EnumSet<Flag> set = flags.length == 0 ? EMPTY : EnumSet.copyOf(Arrays.asList(flags));

			// Verify month
			final boolean leapday = set.contains(Flag.LEAPDAY);
			if(leapday) {
				if(leap) throw new IllegalArgumentException("Month-table can only have one leap-day");
				if(length != 1) throw new IllegalArgumentException("Leap-day month must be one day in length");
				leap = true;
			}

			// Add month entry
			months.add(new Month(name, length, set, day));

			// Accumulate day-of-year
			if(!leapday) {
				day += length;
			}

			return this;
		}

		/**
		 * Constructs this month table.
		 * @return New month table
		 */
		public MonthTable build() {
			if(months.isEmpty()) throw new IllegalArgumentException("Empty month table");
			if(day != (DAYS_IN_YEAR + 1)) throw new IllegalArgumentException(String.format("Invalid month table: actual=%d", day));
			return new MonthTable(months);
		}
	}
}
