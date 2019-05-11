package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class PortalSpellActionTest extends ActionTestBase {
	private PortalSpellAction action;
	private ObjectController controller;
	private Portal portal;

	@BeforeEach
	public void before() {
		// Create a portal
		portal = mock(Portal.class);
		when(portal.name()).thenReturn("portal");
		when(portal.state()).thenReturn(Portal.PortalState.DEFAULT);

		// Create controller for other side of the portal
		controller = mock(ObjectController.class);
		when(controller.other(loc, portal)).thenReturn(loc);

		// Create action
		action = new PortalSpellAction(skill, skill, controller);
	}

	@Test
	public void block() throws ActionException {
		// Cast spell
		addRequiredSkill();
		final Response response = action.block(actor, portal);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction descriptor
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete spell induction
		final Response result = complete(response);
		assertEquals(Response.of(new Description("skill.success", "portal")), result);
		verify(portal).block();
	}

	@Test
	public void breakPortal() throws ActionException {
		addRequiredSkill();
		final Response response = action.breakPortal(actor, portal);
		response.induction().get().induction().complete();
		verify(portal).destroy();
	}

	@Test
	public void castInvalidState() throws ActionException {
		when(portal.state()).thenReturn(Portal.PortalState.BLOCKED);
		TestHelper.expect("skill.invalid", () -> action.block(actor, portal));
	}

	@Test
	public void castFailed() throws ActionException {
		final Response response = action.breakPortal(actor, portal);
		final Response result = response.induction().get().induction().complete();
		assertEquals(Response.of(new Description("skill.failed", "portal")), result);
	}
}
