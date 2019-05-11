package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.MovementController.Result;

public class EncumberanceMovementRequirementTest {
	private EncumberanceMovementRequirement requirement;
	private EncumberanceCalculator calc;
	private Entity actor;

	@BeforeEach
	public void before() {
		calc = mock(EncumberanceCalculator.class);
		requirement = new EncumberanceMovementRequirement(calc);
	}

	@Test
	public void resultNotEncumbered() {
		when(calc.calculate(actor)).thenReturn(Percentile.ZERO);
		assertEquals(Result.DEFAULT, requirement.result(actor, null));
	}

	@Test
	public void resultEncumbered() {
		when(calc.calculate(actor)).thenReturn(Percentile.ONE);
		assertEquals(EncumberanceMovementRequirement.ENCUMBERED, requirement.result(actor, null));
	}

	@Test
	public void resultPartiallyEncumbered() {
		when(calc.calculate(actor)).thenReturn(Percentile.HALF);
		requirement.setEncumberanceModifier(4);
		assertEquals(new Result(null, 2), requirement.result(actor, null));
	}
}
