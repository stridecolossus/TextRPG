package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Terrain;

public class CampActionTest extends ActionTestBase {
	private CampAction action;
	private LightController controller;

	@BeforeEach
	public void before() {
		final Light.Descriptor descriptor = CampAction.descriptor(DURATION, Percentile.HALF, Percentile.HALF);
		controller = mock(LightController.class);
		action = new CampAction(skill, controller, descriptor);
	}

	@Test
	public void isValidTerrain() {
		assertEquals(false, action.isValid(Terrain.URBAN));
		assertEquals(false, action.isValid(Terrain.INDOORS));
	}

	@Test
	public void isValidStance() {
		assertEquals(true, action.isValid(Stance.RESTING));
	}

	@Test
	public void camp() throws ActionException {
		// Start building camp
		final WorldObject wood = mock(WorldObject.class);
		final Response response = action.camp(actor, wood);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete induction
		assertEquals(Response.of("camp.completed"), complete(response));

		// Check fuel consumed
		verify(wood).destroy();

		// Check camp-fire generated in location
		final Light camp = loc.contents().select(Light.class).findAny().orElseThrow();
		assertEquals("campfire", camp.name());
		assertEquals(Light.Type.CAMPFIRE, camp.descriptor().type());
		verify(controller).light(actor, camp);
	}

	@Test
	public void campAlreadyPresent() throws ActionException {
		final Light camp = new Light.Descriptor(ObjectDescriptor.of("camp"), Light.Type.CAMPFIRE, DURATION, Percentile.ONE, Percentile.ONE).create();
		camp.parent(loc);
		camp.light(0);
		TestHelper.expect("camp.already.present", () -> action.camp(actor, null));
	}
}
