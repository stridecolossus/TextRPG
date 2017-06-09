package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.sarge.textrpg.util.Randomiser;

public class RandomiserTest {
	@Test
	public void range() {
		assertEquals(0, Randomiser.range(1));
	}
	
	@Test
	public void randomList() {
		assertEquals(new Integer(42), Randomiser.random(Arrays.asList(42)));
	}
}
