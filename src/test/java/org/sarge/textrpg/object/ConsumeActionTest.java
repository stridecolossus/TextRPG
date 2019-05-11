package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class ConsumeActionTest extends ActionTestBase {
	private ConsumeAction action;
	private Food food;

	@BeforeEach
	public void before() {
		action = new ConsumeAction();
		food = new Food.Descriptor(new ObjectDescriptor.Builder("food").decay(DURATION).build(), 3).create();
	}

	@Test
	public void consume() throws ActionException {
		// Set actor hungry
		actor.model().values().get(EntityValue.HUNGER.key()).set(2);

		// Consume food
		food.parent(TestHelper.parent());
		final Response response = action.consume(actor, food);

		// Check response
		final Response expected = new Response.Builder()
			.add(new Description("action.consume.response", "food"))
			.add("consume.hunger.zero")
			.build();
		assertEquals(expected, response);

		// Check food consumed
		assertEquals(false, food.isAlive());

		// Check hunger reduced
		assertEquals(0, actor.model().values().get(EntityValue.HUNGER.key()).get());
	}

	@Test
	public void consumeNotHungry() throws ActionException {
		TestHelper.expect("consume.not.hungry", () -> action.consume(actor, food));
	}
}
