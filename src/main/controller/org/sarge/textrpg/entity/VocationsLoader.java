package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.entity.Vocation.RecipeBuilder;
import org.sarge.textrpg.object.DefaultObjectDescriptorLoader;
import org.sarge.textrpg.object.LootFactoryLoader;
import org.sarge.textrpg.object.ObjectDescriptorLoader;

/**
 * Loader for vocations and recipes.
 * @author Sarge
 */
public class VocationsLoader {
	private final DefaultObjectDescriptorLoader loader;
	private final LootFactoryLoader factory;

	/**
	 * Constructor.
	 * @param loader		Object descriptor loader
	 * @param factory		Factory loader
	 */
	public VocationsLoader(DefaultObjectDescriptorLoader loader, LootFactoryLoader factory) {
		this.loader = notNull(loader);
		this.factory = notNull(factory);
	}

	/**
	 * Loads vocations.
	 * @param xml XML
	 * @return Vocations
	 */
	public List<Vocation> load(Element xml) {
		return xml.children("vocation").map(this::loadVocation).collect(toList());
	}

	/**
	 * Loads a vocation.
	 */
	private Vocation loadVocation(Element xml) {
		// Load vocation
		final Vocation.Builder builder = new Vocation.Builder()
			.name(xml.attribute("name").toText())
			.tool(xml.attribute("tool").toText());

		// Load optional crafting station
		xml.find("station").map(e -> loader.load(e, ObjectDescriptorLoader.Policy.FIXTURE)).ifPresent(builder::station);

		// Load recipes
		xml.children("tier").forEach(e -> loadTier(e, builder));

		// Construct vocation and recipes
		return builder.build();
	}

	/**
	 * Loads a recipe tier.
	 */
	private void loadTier(Element xml, Vocation.Builder builder) {
		builder.tier();
		xml.children("recipe").map(this::loadRecipe).forEach(builder::add);
	}

	/**
	 * Loads a recipe.
	 */
	private RecipeBuilder loadRecipe(Element xml) {
		// Load recipe
		final RecipeBuilder builder = new RecipeBuilder()
			.name(xml.attribute("name").toText())
			.factory(factory.load(xml.child("factory")));

		// Load ingredients
		xml.child("ingredients")
			.children()
			.map(e -> loader.load(e, ObjectDescriptorLoader.Policy.OBJECT))
			.forEach(builder::ingredient);

		return builder;
	}
}
