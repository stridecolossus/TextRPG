package org.sarge.textrpg.entity;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class EffectMethodTest {
	private Entity entity;
	
	@Before
	public void before() {
		entity = mock(Entity.class);
	}
	
	@Test
	public void attribute() {
		final EffectMethod effect = EffectMethod.attribute(Attribute.AGILITY);
		assertNotNull(effect);
		effect.apply(entity, 42);
		verify(entity).modify(Attribute.AGILITY, 42);
	}
	
	@Test
	public void value() {
		final EffectMethod effect = EffectMethod.value(EntityValue.POWER);
		assertNotNull(effect);
		effect.apply(entity, 42);
		verify(entity).modify(EntityValue.POWER, 42);
	}
}
