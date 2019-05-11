package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.EnumerationIntegerMap;
import org.sarge.textrpg.util.Percentile;

public class EncumberanceCalculatorTest {
	private EncumberanceCalculator calc;
	private Entity actor;
	private EnumerationIntegerMap<Attribute> attrs;

	@BeforeEach
	public void before() {
		// Create attributes
		final EntityModel model = mock(EntityModel.class);
		attrs = new EnumerationIntegerMap<>(Attribute.class);
		when(model.attributes()).thenReturn(attrs);

		// Create inventory
		final Inventory inv = mock(Inventory.class);

		// Create actor
		actor = mock(Entity.class);
		when(actor.model()).thenReturn(model);
		when(actor.contents()).thenReturn(inv);

		// Create calculator
		calc = new EncumberanceCalculator(1, 1);
	}

	@Test
	public void calculateEmptyInventory() {
		attrs.get(Attribute.STRENGTH).set(999);
		assertEquals(Percentile.ZERO, calc.calculate(actor));
	}

	@Test
	public void calculateEncumbered() {
		when(actor.contents().weight()).thenReturn(1);
		attrs.get(Attribute.STRENGTH).set(1);
		assertEquals(Percentile.ONE, calc.calculate(actor));
	}

	@Test
	public void calculateBelowThreshold() {
		when(actor.contents().weight()).thenReturn(2);
		attrs.get(Attribute.STRENGTH).set(5);
		calc.setThreshold(Percentile.HALF);
		assertEquals(Percentile.ZERO, calc.calculate(actor));
	}

	@Test
	public void calculatePartiallyEncumbered() {
		when(actor.contents().weight()).thenReturn(3);
		attrs.get(Attribute.STRENGTH).set(4);
		calc.setThreshold(Percentile.HALF);
		assertEquals(Percentile.HALF, calc.calculate(actor));
	}
}
