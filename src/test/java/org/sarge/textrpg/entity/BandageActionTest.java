package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class BandageActionTest extends ActionTestBase {
	private BandageAction action;

	@BeforeEach
	public void before() {
		action = new BandageAction(skill);
	}

	@Disabled("need to sort applied effects")
	@Test
	public void bandage() throws ActionException {
		// Add wound
		// TODO

		// Start bandaging
		final WorldObject bandages = mock(WorldObject.class);
		final Response response = action.bandage(actor, bandages);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete induction
		final Response result = complete(response);
		assertEquals(Response.of(new Description("bandage.success", actor.name())), result);

	}

	@Test
	public void bandageInvalidTarget() throws ActionException {
		final Entity entity = mock(Entity.class);
		when(actor.isValidTarget(entity)).thenReturn(true);
		TestHelper.expect("bandage.invalid.target", () -> action.bandage(actor, null, entity));
	}

	@Test
	public void bandageFailed() throws ActionException {

	}
}
