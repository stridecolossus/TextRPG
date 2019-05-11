package org.sarge.textrpg.entity;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.entity.Vocation.Recipe;

/**
 * Model for recipes known by a {@link PlayerCharacter}.
 * @author Sarge
 */
public class RecipeModel {
	private final Map<Vocation, List<List<Recipe>>> recipes = new StrictMap<>();

	/**
	 * Vocations in this model.
	 * @return Vocations
	 */
	public Stream<Vocation> vocations() {
		return recipes.keySet().stream();
	}

	/**
	 * Looks up the recipes for the given vocation.
	 * @param vocation Vocation
	 * @return Recipes
	 */
	private List<List<Recipe>> get(Vocation vocation) {
		final var results = recipes.get(vocation);
		if(results == null) throw new IllegalArgumentException("Vocation not present: " + vocation);
		return results;
	}

	/**
	 * Known recipes for the given vocation.
	 * @param vocation Vocation
	 * @return Recipe tiers
	 * @throws IllegalArgumentException if the vocation is not present
	 */
	public Stream<List<Recipe>> recipes(Vocation vocation) {
		final var results = get(vocation);
		return results.stream().map(List::copyOf);
	}

	/**
	 * Known recipes for the given vocation and tier.
	 * @param vocation 		Vocation
	 * @param tier			Tier
	 * @return Recipes
	 * @throws IllegalArgumentException if the vocation/tier is not present
	 */
	public Stream<Recipe> recipes(Vocation vocation, int tier) {
		Check.oneOrMore(tier);
		final var results = get(vocation);
		if(tier > results.size()) throw new IllegalArgumentException(String.format("Vocation tier not present: vocation=%s tier=%d", vocation, tier));
		return results.get(tier - 1).stream();
	}

	/**
	 * Adds a new vocation.
	 * @param vocation Vocation
	 * @throws IllegalArgumentException if the vocation is already present
	 */
	public void add(Vocation vocation) {
		if(recipes.containsKey(vocation)) throw new IllegalArgumentException("Vocation already present: " + vocation);
		recipes.put(vocation, new StrictList<>());
	}

	/**
	 * Adds the next tier for the given vocation.
	 * @throws IllegalArgumentException if the given vocation is not a member of this model or the maximum tier has been reached
	 */
	public void tier(Vocation vocation) {
		final var recipes = get(vocation);
		if(recipes.size() == vocation.tiers()) throw new IllegalArgumentException("Maximum tier for vocation: " + vocation);
		recipes.add(new StrictList<>());
	}

	/**
	 * Adds a recipe.
	 * @param recipe Recipe to add
	 * @throws IllegalArgumentException if the recipe is already present or the vocation/tier is not a member of this model
	 */
	public void add(Recipe recipe) {
		// Lookup vocation
		final var recipes = get(recipe.vocation());

		// Check tier
		final int index = recipe.tier() - 1;
		if(index >= recipes.size()) throw new IllegalArgumentException("Recipe tier not present: " + recipe);

		// Add recipe
		final var tier = recipes.get(index);
		if(tier.contains(recipe)) throw new IllegalArgumentException("Recipe already present: " + recipe);
		tier.add(recipe);
	}
}
