package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PercentileTest {
	private Percentile p;

	@BeforeEach
	public void before() {
		p = new Percentile(0.42f);
	}

	@Test
	public void equals() {
		assertEquals(0.42f, p.floatValue(), 0.0001f);
		assertEquals(new Percentile(0.42f), p);
	}

	@Test
	public void numbers() {
		assertEquals(42, p.intValue());
		assertEquals(42L, p.longValue());
		assertEquals(0.42d, p.doubleValue(), 0.0001d);
	}

	@Test
	public void invert() {
		final Percentile inverted = p.invert();
		assertNotNull(inverted);
		assertEquals(58, inverted.intValue());
	}

	@Test
	public void scale() {
		final Percentile scaled = p.scale(Percentile.HALF);
		assertNotNull(scaled);
		assertEquals(21, scaled.intValue());
	}

	@Test
	public void isZero() {
		assertEquals(true, Percentile.ZERO.isZero());
		assertEquals(false, p.isZero());
	}

	@Test
	public void isLessThan() {
		assertTrue(p.isLessThan(Percentile.HALF));
		assertFalse(Percentile.HALF.isLessThan(p));
	}

	@Test
	public void compare() {
		assertEquals(+1, p.compareTo(Percentile.ZERO));
		assertEquals(-1, p.compareTo(Percentile.ONE));
		assertEquals(0, p.compareTo(p));
	}

	@Test
	public void min() {
		assertEquals(Percentile.HALF, Percentile.HALF.min(Percentile.ONE));
		assertEquals(Percentile.HALF, Percentile.ONE.min(Percentile.HALF));
	}

	@Test
	public void max() {
		assertEquals(Percentile.ONE, Percentile.HALF.max(Percentile.ONE));
		assertEquals(Percentile.ONE, Percentile.ONE.max(Percentile.HALF));
	}

	@Test
	public void string() {
		assertEquals("42%", p.toString());
	}

	@Test
	public void constants() {
		assertEquals(0, Percentile.ZERO.intValue());
		assertEquals(50, Percentile.HALF.intValue());
		assertEquals(100, Percentile.ONE.intValue());
	}

	@Test
	public void ofInteger() {
		assertEquals(Percentile.HALF, Percentile.of(50));
	}

	@Test
	public void ofRange() {
		assertEquals(Percentile.HALF, Percentile.of(5, 10));
	}

	@Test
	public void converter() {
		assertEquals(Percentile.HALF, Percentile.CONVERTER.apply("50"));
		assertEquals(Percentile.HALF, Percentile.CONVERTER.apply("0.5"));
	}
}
