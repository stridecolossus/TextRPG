package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class OpenableActionTest extends ActionTestBase {
	private OpenableAction action;
	private Openable.Model model;
	private ObjectController controller;

	@BeforeEach
	public void before() {
		model = new Openable.Model(new Openable.Lock(ObjectDescriptor.of("key"), Percentile.ONE, null));
		controller = mock(ObjectController.class);
		action = new OpenableAction(controller);
	}

	/**
	 * Adds requires key to the actors inventory.
	 */
	private void key() {
		final ObjectDescriptor key = model.lock().key();
		key.create().parent(actor);
	}

	@Test
	public void executePortal() throws ActionException {
		// Create portal
		final Portal portal = mock(Portal.class);
		when(portal.model()).thenReturn(model);
		when(portal.name()).thenReturn("portal");
		when(controller.other(loc, portal)).thenReturn(loc);

		// Execute
		key();
		final Response response = action.execute(actor, Openable.Operation.UNLOCK, portal);
		assertEquals(Response.OK, response);
		assertEquals(Openable.State.CLOSED, model.state());
	}

	@Test
	public void executeContainer() throws ActionException {
		// Create container
		final OpenableContainer container = mock(OpenableContainer.class);
		when(container.model()).thenReturn(model);
		when(container.name()).thenReturn("container");

		// Execute
		key();
		final Response response = action.execute(actor, Openable.Operation.UNLOCK, container);
		assertEquals(Response.OK, response);
		assertEquals(Openable.State.CLOSED, model.state());
	}

	@Test
	public void executeRequiresKey() throws ActionException {
		final Portal portal = mock(Portal.class);
		when(portal.model()).thenReturn(model);
		TestHelper.expect("openable.requires.key", () -> action.execute(actor, Openable.Operation.UNLOCK, portal));
	}
}
