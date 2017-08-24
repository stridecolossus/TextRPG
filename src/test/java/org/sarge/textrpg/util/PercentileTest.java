package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sarge.lib.object.EqualsBuilder;
import org.sarge.textrpg.util.Percentile;

public class PercentileTest {
	private Percentile percentile;
	
	@Before
	public void before() {
		percentile = new Percentile(0.25f);
	}
	
	@Test
	public void constants() {
		assertEquals(new Percentile(100), Percentile.ONE);
		assertEquals(new Percentile(50), Percentile.HALF);
		assertEquals(new Percentile(0), Percentile.ZERO);
	}

	@Test
	public void floatValue() {
		assertTrue(EqualsBuilder.isEqual(0.25f, percentile.floatValue()));
	}

	@Test
	public void intValue() {
		assertEquals(25, percentile.intValue());
	}
	
	@Test
	public void invert() {
		assertEquals(new Percentile(0.75f), percentile.invert());
	}
	
	@Test
	public void isLessThan() {
		assertEquals(true, percentile.isLessThan(Percentile.HALF));
		assertEquals(false, percentile.isLessThan(Percentile.ZERO));
	}
	
	@Test
	public void equals() {
		assertEquals(new Percentile(0.25f), percentile);
		assertEquals(percentile, new Percentile(0.25f));
		assertNotEquals(percentile, null);
	}
	
	@Test
	public void converter() {
		assertEquals(Percentile.HALF, Percentile.CONVERTER.convert("0.5"));
	}
}
