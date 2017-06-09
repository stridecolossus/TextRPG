package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SizeTest {
	@Test
	public void invert() {
		assertEquals(Size.HUGE, Size.TINY.invert());
		assertEquals(Size.LARGE, Size.SMALL.invert());
		assertEquals(Size.MEDIUM, Size.MEDIUM.invert());
		assertEquals(Size.SMALL, Size.LARGE.invert());
		assertEquals(Size.TINY, Size.HUGE.invert());
	}
	
	@Test(expected = RuntimeException.class)
	public void invertNone() {
		Size.NONE.invert();
	}
	
	@Test
	public void isLarger() {
		assertEquals(true, Size.TINY.isLargerThan(Size.NONE));
		assertEquals(true, Size.SMALL.isLargerThan(Size.TINY));
		assertEquals(true, Size.MEDIUM.isLargerThan(Size.SMALL));
		assertEquals(true, Size.LARGE.isLargerThan(Size.MEDIUM));
		assertEquals(true, Size.HUGE.isLargerThan(Size.LARGE));
	}
}
