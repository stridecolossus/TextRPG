package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

/**
 * Crafting vocation.
 */
public final class Vocation extends AbstractEqualsObject {
	/**
	 * Crafting recipe for this vocation.
	 */
	public final class Recipe extends AbstractEqualsObject implements CommandArgument {
		private final String recipe;
		private final Vocation vocation;
		private final int tier;
		private final Collection<ObjectDescriptor.Filter> ingredients;
		private final LootFactory factory;

		/**
		 * Constructor.
		 * @param name				Recipe name
		 * @param vocation			Vocation
		 * @param tier				Vocation tier 1..n
		 * @param ingredients		Ingredients
		 * @param factory			Output factory
		 */
		public Recipe(String name, Vocation vocation, int tier, Collection<ObjectDescriptor> ingredients, LootFactory factory) {
			Check.notEmpty(ingredients);
			this.recipe = notEmpty(name);
			this.vocation = notNull(vocation);
			this.tier = oneOrMore(tier);
			this.ingredients = List.copyOf(ingredients.stream().map(ObjectDescriptor.Filter::of).collect(toList()));
			this.factory = notNull(factory);
		}

		/**
		 * @return Recipe name
		 */
		@Override
		public String name() {
			return recipe;
		}

		/**
		 * @return Vocation
		 */
		public Vocation vocation() {
			return vocation;
		}

		/**
		 * @return Recipe tier 1..n
		 */
		public int tier() {
			return tier;
		}

		/**
		 * @return Ingredients
		 */
		public Collection<ObjectDescriptor.Filter> ingredients() {
			return ingredients;
		}

		/**
		 * @return Output factory
		 */
		public LootFactory factory() {
			return factory;
		}
	}

	private final String name;
	private final WorldObject.Filter filter;
	private final Optional<ObjectDescriptor> station;
	private final List<List<Recipe>> recipes = new StrictList<>();

	/**
	 * Constructor.
	 * @param name			Vocation name
	 * @param tool			Tool category
	 * @param station		Optional crafting station
	 */
	public Vocation(String name, String tool, ObjectDescriptor station) {
		this.name = notEmpty(name);
		this.filter = WorldObject.Filter.of(tool);
		this.station = Optional.ofNullable(station);
	}

	/**
	 * @return Vocation name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Tool filter
	 */
	public WorldObject.Filter filter() {
		return filter;
	}

	/**
	 * @return Crafting station
	 */
	public Optional<ObjectDescriptor> station() {
		return station;
	}

	/**
	 * @return Number of tiers
	 */
	public int tiers() {
		return recipes.size();
	}

	/**
	 * @return Recipes
	 */
	public Stream<List<Recipe>> recipes() {
		return recipes.stream();
	}

	/**
	 * Builder for a vocation.
	 */
	public static class Builder {
		private String name;
		private String tool;
		private ObjectDescriptor station;

		private final Deque<List<RecipeBuilder>> recipes = new ArrayDeque<>();

		/**
		 * Sets the name of this vocation.
		 * @param name Vocation name
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the tool category required for this vocation.
		 * @param tool Tool category
		 */
		public Builder tool(String tool) {
			this.tool = tool;
			return this;
		}

		/**
		 * Sets the crafting station required for this vocation.
		 * @param station Crafting station descriptor
		 */
		public Builder station(ObjectDescriptor station) {
			this.station = station;
			return this;
		}

		/**
		 * Starts the next tier of recipes.
		 */
		public Builder tier() {
			recipes.addLast(new ArrayList<>());
			return this;
		}

		/**
		 * Adds a recipe to the current tier.
		 * @param builder Recipe builder
		 */
		public Builder add(RecipeBuilder builder) {
			if(recipes.isEmpty()) {
				tier();
			}
			recipes.getLast().add(builder);
			return this;
		}

		/**
		 * Constructs this vocation.
		 * @return New vocation
		 */
		public Vocation build() {
			// Build vocation
			final Vocation vocation = new Vocation(name, tool, station);

			// Build recipes
			for(List<RecipeBuilder> list : recipes) {
				final int level = vocation.recipes.size() + 1;
				final var tier = list.stream().map(b -> b.build(vocation, level)).collect(toList());
				if(tier.isEmpty()) throw new IllegalArgumentException(String.format("Empty recipe tier: vocation=%s tier=%d", name, level));
				vocation.recipes.add(tier);
			}
			if(vocation.recipes.isEmpty()) throw new IllegalArgumentException("No recipes for vocation: " + name);

			return vocation;
		}
	}

	/**
	 * Builder for a recipe.
	 */
	public static class RecipeBuilder {
		private String name;
		private final Collection<ObjectDescriptor> ingredients = new StrictSet<>();
		private LootFactory factory;

		/**
		 * Sets the name of this recipe.
		 * @param name Recipe name
		 */
		public RecipeBuilder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Adds an ingredient of this recipe.
		 * @param ingredient Ingredient descriptor
		 */
		public RecipeBuilder ingredient(ObjectDescriptor ingredient) {
			ingredients.add(ingredient);
			return this;
		}

		/**
		 * Sets the factory of this recipe.
		 * @param factory Factory
		 */
		public RecipeBuilder factory(LootFactory factory) {
			this.factory = factory;
			return this;
		}

		/**
		 * Constructs a new recipe for the given vocation.
		 */
		private Recipe build(Vocation vocation, int level) {
			return vocation.new Recipe(name, vocation, level, ingredients, factory);
		}
	}
}
