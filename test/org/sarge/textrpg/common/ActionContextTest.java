package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.entity.EntityValueCalculator;
import org.sarge.textrpg.entity.MovementController;
import org.sarge.textrpg.object.PostManager;

public class ActionContextTest {
	private ActionContext ctx;
	
	@Before
	public void before() {
		ctx = new ActionContext(mock(TimeCycle.class), mock(EntityValueCalculator.class), mock(MovementController.class), mock(PostManager.class));
	}
	
	@Test
	public void constructor() {
		assertNotNull(ctx.getTimeCycle());
		assertNotNull(ctx.getPostManager());
	}
	
	@Test
	public void update() {
		final EventQueue queue = mock(EventQueue.class);
		final long now = System.currentTimeMillis();
		ctx.add(queue);
		ctx.update(now);
		verify(queue).update(now);
	}
	
	@Test
	public void isDayLight() {
		Clock.CLOCK.setDateTime(LocalDateTime.of(1, 1, 1, 0, 0));
		assertEquals(false, ctx.isDaylight());
		Clock.CLOCK.setDateTime(LocalDateTime.of(1, 1, 1, 12, 0));
		assertEquals(true, ctx.isDaylight());
	}
}
