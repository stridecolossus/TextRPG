package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MonthTableTest {
	private MonthTable.Builder builder;

	@BeforeEach
	public void before() {
		builder = new MonthTable.Builder();
	}

	@Test
	public void builder() {
		builder.add(100, "start");
		builder.add(100, "middle");
		builder.add(165, "end");
		final MonthTable table = builder.build();
		assertNotNull(table);
		check("start", table, 1);
		check("start", table, 100);
		check("middle", table, 101);
		check("middle", table, 200);
		check("end", table, 201);
		check("end", table, 365);
	}

	private static void check(String expected, MonthTable table, int day) {
		final MonthTable.Month month = table.get(day, false);
		assertNotNull(month);
		assertEquals(expected, month.name());
	}

	@Test
	public void get() {
		final MonthTable table = builder.add(365, "month", MonthTable.Flag.SPECIAL).build();
		final MonthTable.Month month = table.get(1, false);
		assertEquals("month", month.name());
		assertEquals(365, month.length());
		assertEquals(true, month.isFlag(MonthTable.Flag.SPECIAL));
		assertEquals(false, month.isFlag(MonthTable.Flag.LEAPDAY));
	}

	@Test
	public void getLeapYearDay() {
		// Create a calendar with a leap-day
		builder.add(100, "before");
		builder.add(1, "leap", MonthTable.Flag.LEAPDAY, MonthTable.Flag.SPECIAL);
		builder.add(265, "after");
		final MonthTable table = builder.build();

		// Lookup leap-year month
		final MonthTable.Month leap = table.get(101, true);
		assertEquals("leap", leap.name());
		assertEquals(1, leap.length());
		assertEquals(true, leap.isFlag(MonthTable.Flag.LEAPDAY));
		assertEquals(101, leap.day());

		// Lookup non-leap-year month
		final MonthTable.Month after = table.get(101, false);
		assertEquals("after", after.name());
		assertEquals(false, after.isFlag(MonthTable.Flag.LEAPDAY));
		assertEquals(101, after.day());
	}

	@Test
	public void getInvalid() {
		final MonthTable table = builder.add(365, "month").build();
		assertThrows(IllegalArgumentException.class, () -> table.get(0, false));
		assertThrows(IllegalArgumentException.class, () -> table.get(367, false));
	}

	@Test
	public void buildIncomplete() {
		builder.add(1, "month");
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	public void buildTooManyDays() {
		builder.add(366, "month");
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	public void buildEmpty() {
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
}
