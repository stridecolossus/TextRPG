package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class EnglishNumericFormatterTest {
	private NumericFormatter formatter;
	
	@Before
	public void before() {
		formatter = new EnglishNumericFormatter();
	}
	
	@Test
	public void formatInteger() {
		// Zero
		assertEquals("zero", formatter.format(0));
		
		// Single digit
		assertEquals("one", formatter.format(1));
		assertEquals("two", formatter.format(2));
		
		// Teens
		assertEquals("eleven", formatter.format(11));
		assertEquals("nineteen", formatter.format(19));
		
		// Twenties
		assertEquals("twenty", formatter.format(20));
		assertEquals("twenty one", formatter.format(21));
		
		// Thirties
		assertEquals("thirty", formatter.format(30));
		assertEquals("thirty one", formatter.format(31));
		
		// Hundreds
		assertEquals("one hundred", formatter.format(100));
		assertEquals("one hundred and one", formatter.format(101));
		assertEquals("one hundred and eleven", formatter.format(111));
		assertEquals("one hundred and twenty one", formatter.format(121));
		assertEquals("seven hundred and seventy seven", formatter.format(777));
	}
	
	@Test
	public void formatString() {
		assertEquals(1, formatter.format("one"));
		assertEquals(11, formatter.format("eleven"));
		assertEquals(21, formatter.format("twenty-one"));
		assertEquals(21, formatter.format("twenty-one"));
		assertEquals(311, formatter.format("three hundred and eleven"));
		assertEquals(321, formatter.format("three hundred and twenty one"));
	}
}
