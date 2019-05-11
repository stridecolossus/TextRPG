package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CalculationTest {
	private Calculation two, three;

	@BeforeEach
	public void before() {
		two = Calculation.literal(2);
		three = Calculation.literal(3);
	}

	@Test
	public void zero() {
		assertEquals(0, Calculation.ZERO.evaluate(null));
	}

	@Test
	public void literal() {
		final var literal = Calculation.literal(42);
		assertEquals(42, literal.evaluate(null));
	}

	@Test
	public void random() {
		final var random = Calculation.random(1, 1);
		assertEquals(1, random.evaluate(null));
	}

	@Test
	public void scaled() {
		final Calculation delegate = Calculation.literal(2);
		final Calculation scaled = Calculation.scaled(delegate, 3);
		assertEquals(2 * 3, scaled.evaluate(null));
	}

	@Test
	public void sum() {
		final var sum = Calculation.compound(List.of(two, three), Calculation.Operator.SUM);
		assertEquals(2 + 3, sum.evaluate(null));
	}

	@Test
	public void multiply() {
		final var multiply = Calculation.compound(List.of(two, three), Calculation.Operator.MULTIPLY);
		assertEquals(2 * 3, multiply.evaluate(null));
	}

	@Test
	public void percentile() {
		final Calculation value = Calculation.percentile(src -> 25);
		assertEquals(0.75f, value.evaluate(null), 0.001f);
	}
}
