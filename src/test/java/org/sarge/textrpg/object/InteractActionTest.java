package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class InteractActionTest extends ActionTestBase {
	private InteractAction action;

	@BeforeEach
	public void before() {
		action = new InteractAction();
	}

	@Test
	public void interactControl() throws ActionException {
		final Control control = mock(Control.class);
		when(control.interact(actor, Interaction.PRESS)).thenReturn(Description.of("press"));
		assertEquals(Response.of("press"), action.interact(actor, Interaction.PRESS, control));
		verify(control).interact(actor, Interaction.PRESS);
	}

	@Test
	public void interactObject() {
		TestHelper.expect("control.interaction.none", () -> action.interact(Interaction.PRESS, mock(WorldObject.class)));
	}
}
