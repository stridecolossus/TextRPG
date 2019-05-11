package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.Vocation.Recipe;
import org.sarge.textrpg.entity.Vocation.RecipeBuilder;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;

public class VocationTest {
	private Vocation vocation;

	@BeforeEach
	public void before() {
		final RecipeBuilder recipe = new RecipeBuilder()
			.name("recipe")
			.ingredient(ObjectDescriptor.of("ingredient"))
			.factory(mock(LootFactory.class));

		vocation = new Vocation.Builder()
			.name("vocation")
			.tool("tool")
			.station(ObjectDescriptor.of("station"))
			.add(recipe)
			.build();
	}

	@Test
	public void constructor() {
		assertEquals("vocation", vocation.name());
		assertNotNull(vocation.filter());
		assertEquals(Optional.of(ObjectDescriptor.of("station")), vocation.station());
		assertEquals(1, vocation.tiers());
	}

	@Test
	public void recipe() {
		assertNotNull(vocation.recipes());
		assertEquals(1, vocation.recipes().count());
		assertEquals(1, vocation.recipes().iterator().next().size());
		final Recipe recipe = vocation.recipes().iterator().next().get(0);
		assertEquals("recipe", recipe.name());
		assertEquals(vocation, recipe.vocation());
		assertEquals(1, recipe.tier());
		assertNotNull(recipe.ingredients());
		assertEquals(1, recipe.ingredients().size());
		assertNotNull(recipe.factory());
	}

	@Test
	public void buildEmptyRecipes() {
		final Vocation.Builder builder = new Vocation.Builder().name("vocation").tool("tool");
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	public void buildEmptyRecipeTier() {
		final Vocation.Builder builder = new Vocation.Builder().name("vocation").tool("tool").tier();
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
}
