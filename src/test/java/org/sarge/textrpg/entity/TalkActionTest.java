package org.sarge.textrpg.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.util.ActionException;

public class TalkActionTest extends ActionTestBase {
	private TalkAction action;

	@BeforeEach
	public void before() {
		action = new TalkAction();
	}

	@Test
	public void talk() {
//		final Entity entity = mock(Entity.class);
//		final Race race = new Race.Builder("race").vocation("vocation").build();
//		when(entity.descriptor().race()).thenReturn(race);
//		assertEquals(Response.of("vocation"), action.talk(actor, entity));
	}

	@Test
	public void discuss() throws ActionException {
		// TODO
		action.discuss(actor, null);
	}
}
