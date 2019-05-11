package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.EmissionController;

public class BlowHornActionTest extends ActionTestBase {
	private BlowHornAction action;
	private EmissionController controller;

	@BeforeEach
	public void before() {
		controller = mock(EmissionController.class);
		action = new BlowHornAction(controller);
	}

	@Test
	public void blow() throws ActionException {
		final WorldObject horn = new ObjectDescriptor.Builder("horn").category(BlowHornAction.HORN).size(Size.MEDIUM).build().create();
		final Response response = action.blow(actor, horn);
		assertEquals(Response.of(new Description("action.blow", "horn")), response);
		verify(controller).broadcast(actor, Set.of(new EmissionNotification("horn", Percentile.ONE)));
	}

	@Test
	public void blowInvalidObject() throws ActionException {
		TestHelper.expect("blow.invalid.object", () -> action.blow(actor, mock(WorldObject.class)));
	}

	@Test
	public void blowWrongAlignment() throws ActionException {
		final WorldObject horn = new ObjectDescriptor.Builder("horn").category(BlowHornAction.HORN).alignment(Alignment.GOOD).build().create();
		when(actor.descriptor().alignment()).thenReturn(Alignment.EVIL);
		TestHelper.expect("blow.invalid.alignment", () -> action.blow(actor, horn));
	}
}
