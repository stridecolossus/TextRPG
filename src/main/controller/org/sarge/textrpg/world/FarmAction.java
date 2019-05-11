package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillAction;
import org.sarge.textrpg.entity.CraftAction;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.entity.Vocation.Recipe;
import org.sarge.textrpg.object.ObjectController;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectHelper;
import org.sarge.textrpg.object.Receptacle;
import org.sarge.textrpg.object.ReceptacleController;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.DurationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to farm some crops.
 * @author Sarge
 */
@Component
@RequiresActor
public class FarmAction extends SkillAction {
	/**
	 * Growing crops.
	 */
	private class Crop extends WorldObject {
		private final Recipe recipe;
		private final Actor owner;
		private final Location loc;

		private boolean growing = true;

		/**
		 * Constructor.
		 * @param recipe		Recipe
		 * @param owner			Owner of these crops
		 * @param loc			Location
		 */
		private Crop(Recipe recipe, Actor owner, Location loc) {
			super(create(recipe.name()));
			this.recipe = notNull(recipe);
			this.owner = notNull(owner);
			this.loc = notNull(loc);
			parent(loc);
		}


		@Override
		protected String key(boolean carried) {
			assert !carried;
			if(growing) {
				return "growing";
			}
			else {
				return super.key(carried);
			}
		}

		/**
		 * Finishes growing these crops.
		 */
		private boolean finished() {
			controller.decay(this);
			growing = false;
			return false;
		}

		@Override
		protected void destroy() {
			if(parent() == loc) {
				locations.remove(loc);
			}
			super.destroy();
		}
	}

	/**
	 * Creates crop object descriptor.
	 * @param name Crop name
	 * @return Crop descriptor
	 */
	private ObjectDescriptor create(String name) {
		return new ObjectDescriptor.Builder(name)
			.fixture()
			.decay(decay)
			.reset(decay)
			.build();
	}

	private final ObjectController controller;
	private final ReceptacleController receptacle;

	private final Map<Location, Crop> locations = new HashMap<>();

	private Duration growth = Duration.ofHours(1);
	private Duration decay = Duration.ofHours(1);

	/**
	 * Constructor.
	 * @param skill			Farming skill
	 * @param controller	Object controller
	 * @param receptacle	Receptacle controller
	 */
	public FarmAction(@Value("#{skills.get('farm')}") Skill skill, ObjectController controller, ReceptacleController receptacle) {
		super(skill, Flag.LIGHT, Flag.REVEALS, Flag.INDUCTION, Flag.BROADCAST);
		this.controller = notNull(controller);
		this.receptacle = notNull(receptacle);
	}

	@Override
	public boolean isValid(Terrain terrain) {
		return terrain == Terrain.FARMLAND;
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		// TODO - recipes
		return super.parsers(actor);
	}

	/**
	 * Sets the growing duration.
	 * @param growth Growth duration
	 */
	@Autowired
	public void setGrowthDuration(@Value("${farm.grow.duration}") Duration growth) {
		this.growth = DurationConverter.oneOrMore(growth);
	}

	/**
	 * Sets the crops decay duration.
	 * @param decay Decay duration
	 */
	@Autowired
	public void setDecayDuration(@Value("${farm.decay.duration}") Duration decay) {
		this.decay = DurationConverter.oneOrMore(decay);
	}

	/**
	 * Grows some crops.
	 * @param actor			Actor
	 * @param recipe		Recipe
	 * @return Response
	 * @throws ActionException if there is no available farming spot or the actor does not have sufficient water
	 */
	@RequiredObject("farming.tools")
	public Response farm(Entity actor, Recipe recipe) throws ActionException {
		// Check available spot
		final Location loc = actor.location();
		if(locations.containsKey(loc)) throw ActionException.of("farm.requires.farmland");

		// Check required water
		final Receptacle rec = receptacle.findWater(actor).orElseThrow(() -> ActionException.of("farm.requires.water"));
		if(rec.level() < recipe.tier()) throw ActionException.of("farm.insufficient.water");

		// Check required ingredients
		final Collection<WorldObject> ingredients = new ArrayList<>();
		CraftAction.claim(actor, recipe, ingredients);

		// Create crops and claim location
		final Crop crop = new Crop(recipe, actor, loc);
		locations.put(loc, crop);

		// Start growing
		final Induction induction = new Induction() {
			@Override
			public Response complete() throws ActionException {
				// Consume ingredients
				ingredients.forEach(ObjectHelper::destroy);
				rec.consume(recipe.tier());

				// Register growing finished event
				controller.register(crop::finished, FarmAction.this.growth);

				// Build response
				return Response.of(new Description("farm.planted", recipe.name()));
			}

			@Override
			public void interrupt() {
				locations.remove(loc);
			}
		};
		final Skill skill = super.skill(actor);
		return Response.of(new Induction.Instance(induction, skill.duration()));
	}

	/**
	 * Gathers crops.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if there are no crops to gather
	 */
	public Response gather(Entity actor) throws ActionException {
		// Check can be gathered
		final Location loc = actor.location();
		final Crop crop = locations.get(loc);
		if(crop == null) throw ActionException.of("gather.crops.none");
		if(crop.owner != actor) throw ActionException.of("gather.not.owner");
		if(crop.growing) throw ActionException.of("gather.crops.growing");

		// Remove crops
		crop.destroy();

		// Gather crops
		final InventoryController inv = new InventoryController("gather.crops");
		final var results = inv.take(actor, crop.recipe.factory().generate(actor));

		// Build response
		return Response.of(results);
	}
}
