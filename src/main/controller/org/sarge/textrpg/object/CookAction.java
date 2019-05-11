package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.function.Predicate;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to cook some food.
 * @author Sarge
 */
@Component
public class CookAction extends AbstractAction {
	/**
	 * Matcher for a cooking fire.
	 */
	private static final Predicate<Light> COOKING_FIRE = light -> light.isCategory("cooking.fire") || (light.descriptor().type() == Light.Type.CAMPFIRE);

	private final ObjectController decay;
	private final Duration base;

	/**
	 * Constructor.
	 * @param duration 		Base cooking duration
	 * @param decay			Decay controller
	 */
	public CookAction(@Value("${cook.duration}") Duration duration, ObjectController decay) {
		super(Flag.REVEALS, Flag.INDUCTION, Flag.BROADCAST);
		this.base = notNull(duration);
		this.decay = notNull(decay);
	}

	@Override
	public boolean isValid(Stance stance) {
		if(stance == Stance.RESTING) {
			return true;
		}
		else {
			return super.isValid(stance);
		}
	}

	/**
	 * Cooks some food.
	 * @param actor			Actor
	 * @param food			Food to cook
	 * @return Response
	 * @throws ActionException if the food is already cooked or the actor does not have an equipped cooking utensil
	 */
	@RequiresActor
	public Response cook(Entity actor, @Carried(auto=true) Food food) throws ActionException {
		// Check can be cooked
		if(food.isCookable()) throw ActionException.of("cook.cannot.cook");

		// Check for an equipped utensil
		actor.contents().equipment().select(Utensil.class)
			.filter(utensil -> utensil.contents().isEmpty())
			.findAny()
			.orElseThrow(() -> ActionException.of("cook.requires.utensil"));

		// Check for cooking fire
		check(actor);

		// Cook food
		// TODO - induction
		// TODO - modifier
		final Food cooked = food.cook();
		decay.decay(cooked);

		// Add to inventory
		final InventoryController controller = new InventoryController("take.food");
		final var result = controller.take(actor, cooked);

		// Build response
		return Response.of(result);
	}

	/**
	 * Cooks the ingredients in the given cooking utensil.
	 * @param actor			Actor
	 * @param utensil		Utensil
	 * @return Response
	 * @throws ActionException if the utensil is empty or has no water, or there is no available cooking fire
	 */
	@RequiresActor
	public Response cook(Entity actor, Utensil utensil) throws ActionException {
		// Check ingredients
		if(utensil.contents().isEmpty()) throw ActionException.of("cook.utensil.empty");
		if(!utensil.isWater()) throw ActionException.of("cook.requires.water");
		final Light fire = check(actor);

		// Calculate duration
		final int size = utensil.contents().size();
		final Duration duration = base.multipliedBy(size);
		// TODO - modify by skill, ditto nutrition

		// Create cook induction
		final Induction induction = () -> {
			// Check fire
			// TODO - could be a listener? e.g. could turn off half way through then re-light!
			if(!fire.isActive()) throw ActionException.of("cook.fire.extinguished");

			// Cook meal
			// TODO - seasoning inc nutrition, is a Food or another Utensil flag?
			// TODO - cache meal descriptors?
			final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("broth").weight(size).build();
			final int total = utensil.contents().stream().map(Food.class::cast).map(Food::descriptor).mapToInt(Food.Descriptor::nutrition).sum();
			final Food meal = new Food(new Food.Descriptor(descriptor, total));
			decay.decay(meal);

			// Consume ingredients
			utensil.contents().destroy();
			utensil.water(false);

			// Add meal
			meal.parent(utensil);

			// Build response
			return Response.of("cook.finished");
		};

		// Build response
		return new Response.Builder()
			.add("cook.start")
			.induction(new Induction.Instance(induction, duration))
			.build();
	}

	/**
	 * Checks that a cooking fire is available in the current location.
	 * @param actor Actor
	 * @throws ActionException if there is no active cooking fire in the location
	 */
	private static Light check(Entity actor) throws ActionException {
		return actor.location().contents()
			.select(Light.class)
			.filter(Light::isActive)
			.filter(COOKING_FIRE)
			.findAny()
			.orElseThrow(() -> ActionException.of("cook.requires.fire"));
	}

	/**
	 * Empties the given cooking utensil.
	 * @param actor			Actor
	 * @param utensil		Cooking utensil
	 * @return Response
	 * @throws ActionException if the utensil is already empty
	 */
	@RequiresActor
	public Response empty(Entity actor, Utensil utensil) throws ActionException {
		if(utensil.contents().isEmpty() && !utensil.isWater()) throw ActionException.of("utensil.already.empty");
		// TODO
		//controller.move(utensil.contents().stream(), actor);
		utensil.water(false);
		return Response.OK;
	}
}
