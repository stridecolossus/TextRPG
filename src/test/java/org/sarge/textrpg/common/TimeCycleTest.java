package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.TimeCycle.Period;
import org.sarge.textrpg.util.Percentile;

public class TimeCycleTest {
	private TimeCycle cycle;
	private Period one, two;
	
	@Before
	public void before() {
		one = new Period("1", "one", 0, Percentile.ZERO);
		two = new Period("2", "two", 1, Percentile.HALF);
		cycle = new TimeCycle(Arrays.asList(one, two), 0);
	}
	
	@Test
	public void constructor() {
		assertEquals(one, cycle.getPeriod());
	}
	
	@Test
	public void update() {
		cycle.update(0);
		assertEquals(one, cycle.getPeriod());
		
		cycle.update(1);
		assertEquals(two, cycle.getPeriod());
	}
}
