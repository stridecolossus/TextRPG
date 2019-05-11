package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Vocation;
import org.sarge.textrpg.entity.Vocation.Recipe;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectController;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Receptacle;
import org.sarge.textrpg.object.ReceptacleController;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class FarmActionTest extends ActionTestBase {
	private FarmAction action;
	private ReceptacleController controller;
	private Recipe recipe;
	private ObjectDescriptor ingredient;

	@BeforeEach
	public void before() {
		ingredient = ObjectDescriptor.of("ingredient");
		final Vocation vocation = new Vocation("farming", "tool", null);
		final LootFactory factory = LootFactory.of(ObjectDescriptor.of("crop"), 1);
		recipe = vocation.new Recipe("recipe", vocation, 2, Set.of(ingredient), factory);
		controller = mock(ReceptacleController.class);
		action = new FarmAction(skill, mock(ObjectController.class), controller);
	}

	@Test
	public void isValid() throws ActionException {
		assertEquals(true, action.isValid(Terrain.FARMLAND));
		assertEquals(false, action.isValid(Terrain.DESERT));
	}

	/**
	 * Adds water receptacle with the given level.
	 * @param level Water level
	 */
	private Receptacle addWater(int level) {
		final Receptacle water = new Receptacle.Descriptor(ObjectDescriptor.of("water"), Liquid.WATER, level).create();
		when(controller.findWater(actor)).thenReturn(Optional.of(water));
		return water;
	}

	/**
	 * Adds water receptacle with the expected level.
	 */
	private Receptacle addWater() {
		return addWater(recipe.tier());
	}

	/**
	 * Adds required ingredients.
	 */
	private WorldObject addIngredients() {
		final WorldObject obj = ingredient.create();
		obj.parent(actor);
		return obj;
	}

	@Test
	public void farm() throws ActionException {
		// Add ingredients
		final WorldObject obj = addIngredients();
		final Receptacle water = addWater();

		// Start farming
		final Response response = action.farm(actor, recipe);

		// Complete induction
		final Response result = complete(response);
		assertEquals(Response.of(new Description("farm.planted", "recipe")), result);

		// Check ingredients consumed
		assertEquals(false, obj.isAlive());
		assertEquals(0, water.level());

		// Check crops planted
		assertEquals(1, loc.contents().size());
		final WorldObject crop = (WorldObject) loc.contents().stream().iterator().next();
		assertEquals("recipe", crop.name());
		assertEquals("object.growing", crop.describe(null).key());

		// Complete growing
		// TODO
		/*
		Event.Queue.advance(DURATION.toMillis());
		assertEquals("default", crop.key());

		// Check decays
		Event.Queue.advance(DURATION.toMillis());
		assertEquals(false, crop.isAlive());
		*/
	}

	@Test
	public void farmAlreadyUsed() throws ActionException {
		// Farm
		addWater();
		addIngredients();
		action.farm(actor, recipe);

		// Farm again and check not available
		addWater();
		addIngredients();
		TestHelper.expect("farm.requires.farmland", () -> action.farm(actor, recipe));
	}

	@Test
	public void farmRequiresWater() throws ActionException {
		TestHelper.expect("farm.requires.water", () -> action.farm(actor, recipe));
	}

	@Test
	public void farmInsufficientWater() throws ActionException {
		addWater(1);
		TestHelper.expect("farm.insufficient.water", () -> action.farm(actor, recipe));
	}

	@Test
	public void farmMissingIngredients() throws ActionException {
		addWater();
		TestHelper.expect("craft.insufficient.ingredients", () -> action.farm(actor, recipe));
	}

	private void grow() throws ActionException {
		addWater();
		addIngredients();
		complete(action.farm(actor, recipe));
	}

	@Test
	public void gather() throws ActionException {
		// TODO
		/*
		// Complete growing
		grow();
		Event.Queue.advance(DURATION.toMillis());

		// Gather crops
		final Response response = action.gather(actor);
		// TODO - check response

		// Check crops removed
		assertEquals(true, loc.contents().isEmpty());
		*/
	}

	@Test
	public void gatherNone() throws ActionException {
		TestHelper.expect("gather.crops.none", () -> action.gather(actor));
	}

	@Test
	public void gatherNotOwner() throws ActionException {
		final Entity other = mock(Entity.class);
		when(other.location()).thenReturn(loc);
		grow();
		TestHelper.expect("gather.not.owner", () -> action.gather(other));
	}

	@Test
	public void gatherStillGrowing() throws ActionException {
		grow();
		TestHelper.expect("gather.crops.growing", () -> action.gather(actor));
	}
}
