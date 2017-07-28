package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EnglishFormatterTest {
	@Test
	public void test() {
		final EnglishFormatter formatter = new EnglishFormatter();
		assertEquals("an egg", formatter.format("a egg"));
		assertEquals("this is an egg, that is a cow", formatter.format("this is a egg, that is a cow"));
	}
}
