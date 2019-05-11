package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.AbstractAction.Effort;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class HideActionTest extends ActionTestBase {
	private HideAction action;

	@BeforeEach
	public void before() {
		action = new HideAction(skill);
	}

	@Test
	public void hide() throws ActionException {
		final Response response = action.hide(actor, Effort.NORMAL);
		assertEquals(Response.EMPTY, complete(response));
		verify(actor.model()).stance(Stance.HIDING);
		assertEquals(Percentile.ONE, actor.model().values().visibility().get());
	}

	@Test
	public void hideSneaking() throws ActionException {
		actor.model().values().visibility().stance(Percentile.ONE);
		when(actor.model().stance()).thenReturn(Stance.SNEAKING);
		complete(action.hide(actor, Effort.NORMAL));
		verify(actor.model()).stance(Stance.HIDING);
		assertEquals(Percentile.ONE, actor.model().values().visibility().get());
	}

	@Test
	public void hideAlreadyHidden() throws ActionException {
		when(actor.model().stance()).thenReturn(Stance.HIDING);
		TestHelper.expect("hide.already.hiding", () -> action.hide(actor, Effort.NORMAL));
	}
}
