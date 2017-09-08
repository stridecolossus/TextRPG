package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.entity.Effect.Descriptor;

public class EffectTest extends ActionTest {
	private Descriptor descriptor;
	private Effect effect;
	private EffectMethod method;
	
	@Before
	public void before() {
		method = mock(EffectMethod.class);
		effect = new Effect(method, Value.literal(1), Value.literal(2));
		descriptor = new Descriptor("name", Collections.singletonList(effect));
	}
	
	@Test
	public void constructor() {
		// Check descriptor
		assertEquals("name", descriptor.getName());
		assertNotNull(descriptor.getEffects());
		assertEquals(1, descriptor.getEffects().count());
		
		// Check effect
		assertNotNull(effect.getMethod());
		assertNotNull(effect.getSize());
		assertEquals(1, effect.getSize().evaluate(null));
		assertNotNull(effect.getDuration());
		assertTrue(effect.getDuration().isPresent());
		assertEquals(2, effect.getDuration().get().evaluate(null));
	}
	
	@Test
	public void apply() {
		final Entity entity = mock(Entity.class);
		descriptor.apply(Collections.singletonList(entity), actor);
		verify(entity).apply(method, 1, Optional.of(2), actor.queue());
	}
}
