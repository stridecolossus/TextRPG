package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Calculation;

public class EntityValueCalculatorTest {
	private EntityValueCalculator calc;

	@BeforeEach
	public void before() {
		calc = new EntityValueCalculator.Builder()
			.add(EntityValue.HEALTH, Calculation.literal(1))
			.add(EntityValue.STAMINA, Calculation.literal(2))
			.add(EntityValue.POWER, Calculation.literal(3))
			.build();
	}

	@Test
	public void calculate() {
		assertEquals(1, calc.calculate(EntityValue.HEALTH, null));
		assertEquals(2, calc.calculate(EntityValue.STAMINA, null));
		assertEquals(3, calc.calculate(EntityValue.POWER, null));
	}

	@Test
	public void calculateInvalidKey() {
		assertThrows(IllegalArgumentException.class, () -> calc.calculate(EntityValue.ARMOUR, null));
	}

	@Test
	public void buildIncompleteCalculator() {
		assertThrows(IllegalStateException.class, () -> new EntityValueCalculator.Builder().build());
	}

	@Test
	public void buildInvalidEntityValue() {
		assertThrows(IllegalArgumentException.class, () -> new EntityValueCalculator.Builder().add(EntityValue.ARMOUR, Calculation.ZERO));
	}
}
