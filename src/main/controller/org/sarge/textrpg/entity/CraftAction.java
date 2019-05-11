package org.sarge.textrpg.entity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Vocation.Recipe;
import org.sarge.textrpg.object.DurableObject;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectHelper;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.parser.DefaultArgumentParser;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.LocationCache;
import org.sarge.textrpg.world.Location;
import org.springframework.stereotype.Component;

/**
 * Crafting action.
 * @author Sarge
 */
@Component
public class CraftAction extends AbstractAction {
	private static final WorldObject.Filter CRAFTING_STATION = WorldObject.Filter.of("crafting.station");

	private final LocationCache<ObjectDescriptor> stations = new LocationCache<>(CraftAction::find);

	/**
	 * Constructor.
	 */
	public CraftAction() {
		super(Flag.LIGHT, Flag.REVEALS, Flag.INDUCTION);
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		final PlayerCharacter player = (PlayerCharacter) actor;
		final RecipeModel model = player.player().recipes();
		final CommandArgumentFactory<Recipe> factory = ignore -> model.vocations().flatMap(model::recipes).flatMap(List::stream);
		return ArgumentParser.Registry.of(Recipe.class, new DefaultArgumentParser<>(factory, actor));
	}

	/**
	 * Crafts the given recipe once.
	 * @param actor			Actor
	 * @param recipe		Recipe
	 * @return Response
	 * @throws ActionException if the actor does not have the required crafting skill, lacks the necessary ingredients, or a crafting station is not available
	 */
	@RequiresActor
	public Response craft(PlayerCharacter actor, Recipe recipe) throws ActionException {
		return craft(actor, recipe, 1);
	}

	/**
	 * Crafts the given recipe until all ingredients are consumed.
	 * @param actor			Actor
	 * @param recipe		Recipe
	 * @param filter		Filter (expected to be {@link ObjectDescriptor.Filter#ALL})
	 * @return Response
	 * @throws ActionException if the actor does not have the required crafting skill, lacks the necessary ingredients, or a crafting station is not available
	 */
	@RequiresActor
	public Response craft(PlayerCharacter actor, Recipe recipe, ObjectDescriptor.Filter filter) throws ActionException {
		if(filter != ObjectDescriptor.Filter.ALL) throw new IllegalStateException();
		return craft(actor, recipe, Integer.MAX_VALUE);
	}

	/**
	 * Crafts the given recipe.
	 * @param actor			Actor
	 * @param recipe		Recipe
	 * @param num			Number of times to craft
	 * @return Response
	 * @throws ActionException if the actor lacks the necessary ingredients or a crafting station is not available
	 */
	@RequiresActor
	public Response craft(PlayerCharacter actor, Recipe recipe, Integer num) throws ActionException {
		// Check available station
		final Vocation vocation = recipe.vocation();
		if(vocation.station().isPresent()) {
			check(actor, vocation.station().get());
		}

		// Check crafting tool
		final DurableObject tool = actor.contents().select(DurableObject.class).filter(vocation.filter()).findAny().orElseThrow(() -> ActionException.of("craft.requires.tool"));
		check(tool);

		// Check ingredients
		final Collection<WorldObject> ingredients = new ArrayList<>();
		claim(actor, recipe, ingredients);

		// Create craft induction
		final InventoryController inv = new InventoryController("craft");
		final AtomicInteger count = new AtomicInteger(num);
		final Induction induction = () -> {
			// Consume ingredients
			ingredients.forEach(ObjectHelper::destroy);
			ingredients.clear();

			// Create output objects and add to inventory
			final var objects = recipe.factory().generate(actor);
			final var results = inv.take(actor, objects);

			// TODO - xp?

			// Apply wear
			tool.use();
			check(tool); // TODO - throws here but should add to response and terminate induction

			// Stop if finished
			if((num > 1) && (count.decrementAndGet() < 1)) {
				throw ActionException.of("action.craft.finished");
			}

			// Check ingredients for next iteration
			// TODO - interrupts before response!
			claim(actor, recipe, ingredients);

			// Build response
			return Response.of(results);
		};

		// Create induction
		final var builder = new Induction.Descriptor.Builder()
			.flag(Induction.Flag.SPINNER)
			.period(Duration.ofSeconds(5));			// TODO

		// Set repeating if multiple items to craft
		if(num > 1) {
			builder.flag(Induction.Flag.REPEATING);
		}

		// Build response
		return Response.of(new Induction.Instance(builder.build(), induction));
	}

	/**
	 * @throws ActionException if the given crafting tool is broken
	 */
	private static void check(DurableObject tool) throws ActionException {
		if(tool.isBroken()) throw ActionException.of("craft.tool.broken");
	}

	/**
	 * Helper - Claims ingredients for the next iteration.
	 * @param actor				Actor
	 * @param recipe			Recipe
	 * @param ingredients		List of ingredients
	 * @throws ActionException if the actor does have the necessary ingredients
	 */
	public static void claim(Entity actor, Recipe recipe, Collection<WorldObject> ingredients) throws ActionException {
		assert ingredients.isEmpty();
		final var inv = actor.contents();
		for(ObjectDescriptor.Filter matcher : recipe.ingredients()) {
			final var found = inv.select(WorldObject.class).filter(obj -> matcher.test(obj.descriptor())).findAny().orElseThrow(() -> ActionException.of("craft.insufficient.ingredients"));
			ingredients.add(found);
		}
	}

	/**
	 * Matcher for crafting stations.
	 */
	private static Optional<ObjectDescriptor> find(Location loc) {
		return loc.contents()
			.select(WorldObject.class)
			.filter(CRAFTING_STATION)
			.findAny()
			.map(WorldObject::descriptor);
	}

	/**
	 * Checks that the location contains the required crafting station.
	 */
	private void check(Entity actor, ObjectDescriptor station) throws ActionException {
		stations.find(actor.location())
			.filter(descriptor -> descriptor == station)
			.orElseThrow(() -> ActionException.of("craft.requires.station"));
	}
}
