package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StanceTest {
	@Test
	public void isValidTransition() {
		// Rest
		assertEquals(true, Stance.RESTING.isValidTransition(Stance.DEFAULT));
		assertEquals(true, Stance.RESTING.isValidTransition(Stance.SLEEPING));

		// Mount
		assertEquals(true, Stance.MOUNTED.isValidTransition(Stance.DEFAULT));
		
		// Sleep
		assertEquals(true, Stance.SLEEPING.isValidTransition(Stance.DEFAULT));
		assertEquals(true, Stance.SLEEPING.isValidTransition(Stance.RESTING));
		
		// Sneak
		assertEquals(true, Stance.SNEAKING.isValidTransition(Stance.DEFAULT));
	}
}
