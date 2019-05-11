package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.time.LocalDate;
import java.util.StringJoiner;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Game calendar.
 * @author Sarge
 */
public interface Calendar {
	/**
	 * @return Name of this calendar
	 */
	String name();

	/**
	 * Creates a descriptor for the given date according to this calendar.
	 * @param date Date
	 * @return Day descriptor
	 */
	Date date(LocalDate date);

	/**
	 * @param year Year
	 * @return Whether the given year is a leap-year according to this calendar
	 */
	boolean isLeapYear(int year);

	/**
	 * Description of a date in this calendar.
	 */
	final class Date extends AbstractEqualsObject {
		private final int weekday;
		private final int day;
		private final String month;
		private final int year;

		/**
		 * Constructor.
		 * @param weekday		Day-of-week
		 * @param day			Day-of-month
		 * @param month			Month
		 * @param year			Year
		 */
		public Date(int weekday, int day, String month, int year) {
			this.weekday = oneOrMore(weekday);
			this.day = oneOrMore(day);
			this.month = notEmpty(month);
			this.year = zeroOrMore(year);
		}

		/**
		 * Constructor for a special month without a specific day-of-month or day-of-week.
		 * @param month		Month
		 * @param year		Year
		 */
		public Date(String month, int year) {
			this.weekday = 0;
			this.day = 0;
			this.month = notEmpty(month);
			this.year = zeroOrMore(year);
		}

		/**
		 * @return Whether this is a special day
		 */
		public boolean isSpecial() {
			return day == 0;
		}

		/**
		 * @return Day-of-week
		 */
		public int weekday() {
			return weekday;
		}

		/**
		 * @return Day-of-month
		 */
		public int day() {
			return day;
		}

		/**
		 * @return Month name
		 */
		public String month() {
			return month;
		}

		/**
		 * @return Year
		 */
		public int year() {
			return year;
		}

		@Override
		public String toString() {
			final StringJoiner str = new StringJoiner("-");
			str.add(String.valueOf(day));
			str.add(month);
			str.add(String.valueOf(year));
			return str.toString();
		}
	}
}
