package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Clock;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.entity.Effect.Descriptor;
import org.sarge.textrpg.entity.EffectMethod;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.world.Location;

/**
 * Action to drink from a {@link Receptacle}.
 * @author Sarge
 */
public class DrinkAction extends AbstractAction {
	/**
	 * Matcher for water receptacles.
	 */
	private static final Predicate<WorldObject> WATER = ContentsHelper.receptacleMatcher(Liquid.WATER);

	private final Clock clock;

	public DrinkAction(Clock clock) {
		this.clock = notNull(clock);
	}

	@Override
	public boolean isCombatBlockedAction() {
		return false;
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}

	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Drinks from available water source in the current location or inventory.
	 */
	public ActionResponse drink(Entity actor) throws ActionException {
		// Look for receptacle in location first
		final Location loc = actor.location();
		if(loc.isLightAvailable(clock.isDaylight())) {
			if(loc.isProperty(Location.Property.WATER)) {
				// Drink from global source
				return drink(Receptacle.WATER, actor, "drink.response.water");
			}
			else {
				// Find receptacle in location
				final Optional<Receptacle> rec = ContentsHelper.select(loc.contents().stream(), Receptacle.class).filter(WATER).findFirst();
				if(rec.isPresent()) {
					return drink(rec.get(), actor, "drink.response.receptacle");
				}
			}
		}

		// Otherwise find in inventory
		return drinkInventoryReceptacle(actor);
	}

	private ActionResponse drinkInventoryReceptacle(Entity actor) throws ActionException {
		final Receptacle rec = (Receptacle) find(actor, WATER, false, "water");
		return drink(rec, actor, "drink.response.receptacle");
	}

	/**
	 * Drinks from the given receptacle.
	 * @throws ActionException if the receptacle cannot be drunk from or is empty
	 */
	public ActionResponse drink(Entity actor, Receptacle rec) throws ActionException {
		if(!rec.descriptor().liquid().isDrinkable()) throw new ActionException("drink.not.drinkable");
		if(rec.level() == 0) throw new ActionException("drink.empty.receptacle");
		return drink(rec, actor, "drink.response.liquid");
	}

	/**
	 * Drinks from the given receptacle.
	 * @param rec		Receptacle
	 * @param actor		Actor
	 * @param key		Response key
	 * @throws ActionException if not thirsty
	 */
	private static ActionResponse drink(Receptacle rec, Entity actor, String key) throws ActionException {
		// Drink from receptacle
		final Liquid liquid = rec.descriptor().liquid();
		if(liquid == Liquid.WATER) {
			// Check actor is thirsty
			final int required = actor.values().get(EntityValue.THIRST);
			if(required < 1) throw new ActionException("drink.not.thirsty");

			// Quench thirst
			// TODO - seems very convoluted to construct an effect just to get access to the thirst value?
			final int actual = rec.consume(required);
			final Effect drink = new Effect(EffectMethod.value(EntityValue.THIRST), Value.literal(actual), null);
			final Descriptor effect = new Descriptor("drink", Collections.singletonList(drink));
			effect.apply(Collections.singletonList(actor), actor);
		}
		else {
			// Consume one unit of the liquid
			rec.consume(1);

			// Apply effects
			liquid.effects().apply(Collections.singletonList(actor), actor);
		}

		// Build response
		final Description.Builder desc = new Description.Builder(key).wrap("name", "liquid." + liquid);
		if(rec.level() == 0) {
			desc.add(new Description("drink.empty", "name", rec));
		}
		return new ActionResponse(desc.build());
	}
}
