package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.ExtendedLink;
import org.sarge.textrpg.world.HiddenLink;

public class HiddenLinkActionTest extends ActionTestBase {
	private HiddenLinkAction action;
	private HiddenLink link;

	@BeforeEach
	public void before() {
		link = new HiddenLink(new ExtendedLink.Properties(), "hidden", Percentile.HALF);
		final ExitMap exits = ExitMap.of(Exit.of(Direction.EAST, link, loc));
		when(loc.exits()).thenReturn(exits);
		action = new HiddenLinkAction(DURATION);
	}

	@Test
	public void open() throws ActionException {
		assertEquals(Response.OK, action.reveal(actor, link));
	}

	@Test
	public void openAlreadyKnown() throws ActionException {
		when(actor.perceives(link.controller().get())).thenReturn(true);
		TestHelper.expect("reveal.already.known", () -> action.reveal(actor, link));
	}
}
