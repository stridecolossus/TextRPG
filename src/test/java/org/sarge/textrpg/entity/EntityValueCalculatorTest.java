package org.sarge.textrpg.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.util.MutableIntegerMap;

public class EntityValueCalculatorTest {
	private EntityValueCalculator calc;
	
	@Before
	public void before() {
		calc = new EntityValueCalculator();
	}
	
	@Test
	public void init() {
		// Create an entity with some attributes
		final Entity e = mock(Entity.class);
		final MutableIntegerMap<Attribute> attrs = new MutableIntegerMap<>(Attribute.class);
		attrs.set(Attribute.ENDURANCE, 2);
		attrs.set(Attribute.STRENGTH, 3);
		when(e.attributes()).thenReturn(attrs);

		// Init empty values
		final MutableIntegerMap<EntityValue> values = new MutableIntegerMap<>(EntityValue.class);
		when(e.values()).thenReturn(values);

		// Add some modifier
		calc.add(EntityValue.HEALTH, Attribute.ENDURANCE, 4);
		calc.add(EntityValue.HEALTH, Attribute.STRENGTH, 5);
		
		// Init values and check values modified
		calc.init(e);
		verify(e).modify(EntityValue.HEALTH, 2 * 4);
		verify(e).modify(EntityValue.HEALTH, 3 * 5);
		verify(e).modify(EntityValue.MAX_HEALTH, 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addInvalidEntityValue() {
		calc.add(EntityValue.HUNGER, Attribute.ENDURANCE, 3);
	}
}
