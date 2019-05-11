package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.Vocation.Recipe;
import org.sarge.textrpg.entity.Vocation.RecipeBuilder;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;

public class RecipeModelTest {
	private RecipeModel model;
	private Vocation vocation;
	private Recipe recipe;

	@BeforeEach
	public void before() {
		// Create recipe
		final RecipeBuilder builder = new RecipeBuilder()
			.name("recipe")
			.ingredient(ObjectDescriptor.of("ingredient"))
			.factory(mock(LootFactory.class));

		// Create vocation
		vocation = new Vocation.Builder()
			.name("vocation")
			.tool("tool")
			.station(ObjectDescriptor.of("station"))
			.add(builder)
			.build();

		// Lookup recipe
		recipe = vocation.recipes().iterator().next().iterator().next();

		// Create empty model
		model = new RecipeModel();
	}

	@Test
	public void constructor() {
		assertNotNull(model.vocations());
		assertEquals(0, model.vocations().count());
	}

	@Test
	public void recipesVocationNotPresent() {
		assertThrows(IllegalArgumentException.class, () -> model.recipes(vocation));
	}

	@Test
	public void recipesTierNotPresent() {
		model.add(vocation);
		assertThrows(IllegalArgumentException.class, () -> model.recipes(vocation, 1));
	}

	@Test
	public void addVocation() {
		model.add(vocation);
		assertEquals(1, model.vocations().count());
		assertEquals(vocation, model.vocations().iterator().next());
		assertEquals(0, model.recipes(vocation).count());
	}

	@Test
	public void addVocationAlreadyPresent() {
		model.add(vocation);
		assertThrows(IllegalArgumentException.class, () -> model.add(vocation));
	}

	@Test
	public void addTier() {
		model.add(vocation);
		model.tier(vocation);
		assertEquals(0, model.recipes(vocation, 1).count());
	}

	@Test
	public void addTierVocationNotPresent() {
		assertThrows(IllegalArgumentException.class, () -> model.tier(vocation));
	}

	@Test
	public void addTierMaximum() {
		model.add(vocation);
		model.tier(vocation);
		assertThrows(IllegalArgumentException.class, () -> model.tier(vocation));
	}

	@Test
	public void addRecipe() {
		model.add(vocation);
		model.tier(vocation);
		model.add(recipe);
		assertEquals(List.of(recipe), model.recipes(vocation).iterator().next());
		assertEquals(recipe, model.recipes(vocation, 1).iterator().next());
	}

	@Test
	public void addRecipeAlreadyPresent() {
		model.add(vocation);
		model.tier(vocation);
		model.add(recipe);
		assertThrows(IllegalArgumentException.class, () -> model.add(recipe));
	}

	@Test
	public void addRecipeVocationNotPresent() {
		assertThrows(IllegalArgumentException.class, () -> model.add(recipe));
	}

	@Test
	public void addRecipeTierNotPresent() {
		model.add(vocation);
		assertThrows(IllegalArgumentException.class, () -> model.add(recipe));
	}
}
