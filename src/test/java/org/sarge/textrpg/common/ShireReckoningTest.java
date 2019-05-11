package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.Calendar.Date;

public class ShireReckoningTest {
	private static final int YEAR = 3021;

	private Calendar calendar;

	@BeforeEach
	public void before() {
		calendar = new ShireReckoning();
	}

	@Test
	public void constructor() {
		assertEquals("shire.reckoning", calendar.name());
		assertEquals(false, calendar.isLeapYear(YEAR));
	}

	@Test
	public void day() {
		check("yule.2", 0, Month.DECEMBER, 22);
		check("1", 1, Month.DECEMBER, 23);
		check("6", 2, Month.JUNE, 20);
		check("lithe.1", 0, Month.JUNE, 21);
		check("midyear", 0, Month.JUNE, 22);
		check("lithe.2", 0, Month.JUNE, 23);
		check("7", 1, Month.JUNE, 24);
		check("12", 2, Month.DECEMBER, 20);
		check("yule.1", 0, Month.DECEMBER, 21);
	}

	private void check(String name, int weekday, Month month, int dayOfMonth) {
		// Create date
		final LocalDate local = LocalDate.of(YEAR, month, dayOfMonth);
		final Date date = calendar.date(local);

		// Check date
		assertEquals(name, date.month());
		assertEquals(1421, date.year());

		// Check weekday name
		if(weekday == 0) {
			assertTrue(date.isSpecial());
		}
		else {
			assertEquals(weekday, date.weekday());
		}
	}

	@Test
	public void dayLeapYear() {
		final int year = 3004;
		assertEquals(true, calendar.isLeapYear(year));
		final LocalDate date = LocalDate.of(year, Month.JUNE, 22);
		final Date day = calendar.date(date);
		assertEquals("overlithe", day.month());
	}
}
