package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class StanceActionTest extends ActionTestBase {
	private StanceAction action;

	@BeforeEach
	public void before() {
		action = new StanceAction();
	}

	@Test
	public void stanceAlready() throws ActionException {
		TestHelper.expect("stance.already.default", () -> action.stand(actor));
	}

	@Test
	public void stand() throws ActionException {
		when(actor.model().stance()).thenReturn(Stance.RESTING);
		assertEquals(Response.of("action.stance.default"), action.stand(actor));
		verify(actor.model()).stance(Stance.DEFAULT);
	}

	@Test
	public void rest() throws ActionException {
		assertEquals(Response.of("action.stance.resting"), action.rest(actor));
		verify(actor.model()).stance(Stance.RESTING);
	}

	@Test
	public void sleep() throws ActionException {
		when(actor.model().stance()).thenReturn(Stance.RESTING);
		assertEquals(Response.of("action.stance.sleeping"), action.sleep(actor));
		verify(actor.model()).stance(Stance.SLEEPING);
	}
}
