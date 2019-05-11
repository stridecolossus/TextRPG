package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;

public class SneakActionTest extends ActionTestBase {
	private SneakAction action;

	@BeforeEach
	public void before() {
		action = new SneakAction(skill);
		addRequiredSkill();
	}

	@Test
	public void sneak() throws ActionException {
		final Response response = action.sneak(actor);
		assertEquals(Response.of("action.sneak.start"), response);
		verify(actor.model()).stance(Stance.SNEAKING);
		assertEquals(Percentile.ZERO, actor.model().values().visibility().get());
	}

	@Test
	public void sneakHiding() throws ActionException {
		when(actor.model().stance()).thenReturn(Stance.HIDING);
		actor.model().values().visibility().stance(Percentile.HALF);
		action.sneak(actor);
		assertEquals(Percentile.ZERO, actor.model().values().visibility().get());
	}

	@Test
	public void sneakStop() throws ActionException {
		// Sneak
		action.sneak(actor);
		when(actor.model().stance()).thenReturn(Stance.SNEAKING);

		// Stop sneaking
		final Response response = action.sneak(actor);
		assertEquals(Response.of("action.sneak.stop"), response);
		verify(actor.model()).stance(Stance.DEFAULT);
		assertEquals(Percentile.ONE, actor.model().values().visibility().get());
	}
}
