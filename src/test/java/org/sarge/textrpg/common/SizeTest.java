package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SizeTest {
	@Test
	public void isLargerThan() {
		assertEquals(true, Size.NONE.isLessThan(Size.TINY));
		assertEquals(true, Size.TINY.isLessThan(Size.SMALL));
		assertEquals(true, Size.SMALL.isLessThan(Size.MEDIUM));
		assertEquals(true, Size.MEDIUM.isLessThan(Size.LARGE));
		assertEquals(true, Size.LARGE.isLessThan(Size.HUGE));
	}
}
