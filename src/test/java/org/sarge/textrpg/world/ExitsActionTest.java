package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;

public class ExitsActionTest extends ActionTestBase {
	private ExitsAction action;
	private Exit exit;

	@BeforeEach
	public void before() {
		exit = Exit.of(Direction.EAST, loc);
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		action = new ExitsAction();
	}

	@Test
	public void exits() {
		final Response expected = new Response.Builder()
			.add("list.exits.header")
			.add(exit.describe())
			.build();
		assertEquals(expected, action.exits(actor));
	}

	@Test
	public void exitsNone() {
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		assertEquals(Response.of("list.exits.none"), action.exits(actor));
	}
}
