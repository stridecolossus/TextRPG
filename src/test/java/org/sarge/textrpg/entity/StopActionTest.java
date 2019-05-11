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

public class StopActionTest extends ActionTestBase {
	private StopAction action;

	@BeforeEach
	public void before() {
		action = new StopAction();
	}

	@Test
	public void stop() throws ActionException {
		when(actor.manager().induction().isActive()).thenReturn(true);
		assertEquals(Response.EMPTY, action.stop(actor));
		verify(actor.manager().induction()).interrupt();
	}

	@Test
	public void stopIdle() throws ActionException {
		TestHelper.expect("stop.cannot.interrupt", () -> action.stop(actor));
	}
}
