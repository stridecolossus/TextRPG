package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Calendar.Date;

public class CalendarTest {
	private Date date;

	@BeforeEach
	public void before() {
		date = new Date(1, 2, "month", 1234);
	}

	@Test
	public void constructor() {
		assertEquals(false, date.isSpecial());
		assertEquals(1, date.weekday());
		assertEquals(2, date.day());
		assertEquals("month", date.month());
		assertEquals(1234, date.year());
	}

	@Test
	public void special() {
		date = new Date("month", 1234);
		assertEquals(true, date.isSpecial());
		assertEquals("month", date.month());
		assertEquals(1234, date.year());
	}

	@Test
	public void equals() {
		assertEquals(date, date);
		assertEquals(date, new Date(1, 2, "month", 1234));
		assertNotEquals(date, null);
		assertNotEquals(date, new Date(1, 2, "month", 9999));
	}
}
