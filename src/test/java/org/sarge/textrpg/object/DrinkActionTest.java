package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.EffectController;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class DrinkActionTest extends ActionTestBase {
	private DrinkAction action;
	private ReceptacleController controller;
	private Receptacle rec;

	@BeforeEach
	public void before() {
		controller = mock(ReceptacleController.class);
		action = new DrinkAction(controller, mock(EffectController.class));
		rec = new Receptacle.Descriptor(ObjectDescriptor.of("water"), Liquid.WATER, 2).create();
	}

	@Test
	public void drink() throws ActionException {
		// Make actor thirsty
		rec.parent(actor);
		actor.model().values().get(EntityValue.THIRST.key()).set(1);

		// Add global source
		when(controller.findWater(actor)).thenReturn(Optional.of(rec));

		// Drink from source
		final Response response = action.drink(actor);
		assertNotNull(response);

		// Check water consumed
		assertEquals(1, rec.level());
		assertEquals(0, actor.model().values().get(EntityValue.THIRST.key()).get());
	}

	@Test
	public void drinkReceptacle() throws ActionException {
		// Make actor thirsty
		rec.parent(actor);
		actor.model().values().get(EntityValue.THIRST.key()).set(1);

		// Drink from any available receptacle
		final Response response = action.drink(actor, rec);
		assertNotNull(response);

		// Check water consumed
		assertEquals(1, rec.level());
		assertEquals(0, actor.model().values().get(EntityValue.THIRST.key()).get());
	}

	@Test
	public void drinkNotThirsty() throws ActionException {
		when(controller.findWater(actor)).thenReturn(Optional.of(rec));
		TestHelper.expect("drink.not.thirsty", () -> action.drink(actor));
	}

	@Test
	public void drinkEmptyReceptacle() throws ActionException {
		rec.empty();
		TestHelper.expect("drink.empty.receptacle", () -> action.drink(actor, rec));
	}

	@Test
	public void drinkNotFound() throws ActionException {
		TestHelper.expect("drink.requires.water", () -> action.drink(actor));
	}

	@Test
	public void drinkInvalidReceptacle() throws ActionException {
		rec = new Receptacle.Descriptor(ObjectDescriptor.of("oil"), Liquid.OIL, 2).create();
		TestHelper.expect("drink.cannot.drink", () -> action.drink(actor, rec));
	}
}
