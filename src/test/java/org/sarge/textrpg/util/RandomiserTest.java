package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class RandomiserTest {
	@RepeatedTest(10)
	public void range() {
		final int value = Randomiser.range(42);
		assertTrue((value >= 0) && (value < 42));
	}

	@Test
	public void isLessThan() {
		assertEquals(false, Randomiser.isLessThan(Percentile.ZERO));
	}

	@Test
	public void select() {
		final var list = List.of("element");
		assertEquals("element", Randomiser.select(list));
	}

	@Test
	public void selectEmptyList() {
		assertThrows(NoSuchElementException.class, () -> Randomiser.select(List.of()));
	}
}
