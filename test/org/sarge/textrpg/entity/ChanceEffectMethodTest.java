package org.sarge.textrpg.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.util.Percentile;

public class ChanceEffectMethodTest {
	private EffectMethod delegate;
	
	@Before
	public void before() {
		delegate = mock(EffectMethod.class);
	}
	
	@Test
	public void apply() {
		final EffectMethod effect = new ChanceEffectMethod(delegate, Percentile.ONE);
		effect.apply(null, 1);
		verify(delegate).apply(null, 1);
	}

	@Test
	public void remove() {
		final EffectMethod effect = new ChanceEffectMethod(delegate, Percentile.ZERO);
		effect.apply(null, -1);
		verify(delegate).apply(null, -1);
	}
	
	@Test
	public void applyChance() {
		final EffectMethod effect = new ChanceEffectMethod(delegate, Percentile.ZERO);
		effect.apply(null, 1);
		verifyZeroInteractions(delegate);
	}
}
