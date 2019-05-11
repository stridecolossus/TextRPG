package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.util.EnumerationIntegerMap;
import org.sarge.textrpg.util.Percentile;

public class PerceptionCalculatorTest {
	private PerceptionCalculator controller;
	private Entity actor;

	@BeforeEach
	public void before() {
		controller = new PerceptionCalculator(2);
		actor = mock(Entity.class);
	}

	@Test
	public void score() {
		// Init attribute
		final EntityModel model = mock(EntityModel.class);
		final var attrs = new EnumerationIntegerMap<>(Attribute.class);
		when(actor.model()).thenReturn(model);
		when(model.attributes()).thenReturn(attrs);
		attrs.get(Attribute.PERCEPTION).set(3);

		// Calculate score
		final Percentile score = controller.score(actor);
		assertEquals(Percentile.of(2 * 3), score);
	}

	@Test
	public void filter() {
		final var filter = controller.filter(Percentile.HALF, Hidden::visibility);
		assertEquals(false, filter.test(() -> Percentile.ZERO));
		assertEquals(false, filter.test(() -> Percentile.HALF));
		assertEquals(true, filter.test(() -> Percentile.ONE));
	}
}
