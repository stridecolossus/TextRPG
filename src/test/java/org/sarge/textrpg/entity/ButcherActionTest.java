package org.sarge.textrpg.entity;

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
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.object.ObjectController;
import org.sarge.textrpg.util.ActionException;

public class ButcherActionTest extends ActionTestBase {
	private ButcherAction action;
	private ObjectController controller;

	@BeforeEach
	public void before() {
		controller = mock(ObjectController.class);
		action = new ButcherAction(DURATION, controller);
	}

	@Test
	public void butcher() throws ActionException {
		// Create a corpse
		final Corpse corpse = mock(Corpse.class);
		when(corpse.name()).thenReturn("corpse");
		when(corpse.size()).thenReturn(Size.MEDIUM);

		// Start butchering
		final Response response = action.butcher(actor, corpse);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete induction
		final Response result = complete(response);

		// Check butchered
		verify(corpse).butcher(actor);
		// TODO - check results

		// Check decay registered
	}
}
