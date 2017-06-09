package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.MapBuilder;

public class TableConverterTest {
	private Converter<Integer> converter;
	
	@Before
	public void before() {
		converter = new TableConverter<>(Converter.INTEGER, MapBuilder.build("one", 1, "two", 2));
	}
	
	@Test
	public void convert() {
		assertEquals(new Integer(1), converter.convert("one"));
		assertEquals(new Integer(2), converter.convert("two"));
		assertEquals(new Integer(3), converter.convert("3"));
	}
}
