package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.EffectController;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.ValueModifier;
import org.springframework.stereotype.Component;

/**
 * Drink from a receptacle.
 * @author Sarge
 * TODO - drink beer [with] entity
 */
@Component
public class DrinkAction extends AbstractAction {
	private final ReceptacleController receptacle;
	private final EffectController effects;

	/**
	 * Constructor.
	 * @param receptacle		Receptacle controller
	 * @param effects			Effects controller
	 */
	public DrinkAction(ReceptacleController receptacle, EffectController effects) {
		super(Flag.OUTSIDE);
		this.receptacle = notNull(receptacle);
		this.effects = notNull(effects);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Drinks water from any available receptacle.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if no water source is available
	 */
	@RequiresActor
	public Response drink(Entity actor) throws ActionException {
		final Receptacle rec = receptacle.findWater(actor).orElseThrow(() -> ActionException.of("drink.requires.water"));
		// TODO - check parent-blocked & light-level for global/location source
		return drinkWater(actor, rec);
	}

	/**
	 * Drinks from the given receptacle.
	 * @param actor		Actor
	 * @param rec		Receptacle
	 * @return Response
	 * @throws ActionException if the receptacle cannot be drunk or is empty
	 */
	@RequiresActor
	public Response drink(Entity actor, Receptacle rec) throws ActionException {
		// Check can be drunk
		final Liquid liquid = rec.descriptor().liquid();
		if(liquid == Liquid.OIL) throw ActionException.of("drink.cannot.drink");
		if(rec.level() == 0) throw ActionException.of("drink.empty.receptacle");

		// Drink from receptacle
		if(liquid == Liquid.WATER) {
			return drinkWater(actor, rec);
		}
		else {
			// Apply effects
			effects.apply(actor, liquid.effect(), List.of(actor));
			// TODO - responses?

			// Apply curative
			final Effect.Group type = liquid.curative();
			if(type != Effect.Group.DEFAULT) {
				// TODO
			}

			// Build response
			return response(rec).build();
		}
	}

	/**
	 * Drinks water from the given receptacle.
	 * @param actor		Actor
	 * @param rec		Water receptacle
	 * @return Response
	 * @throws ActionException if the actor is not thirsty
	 */
	private static Response drinkWater(Entity actor, Receptacle rec) throws ActionException {
		// Check whether thirsty
		final ValueModifier thirst = actor.model().values().get(EntityValue.THIRST.key());
		if(thirst.get() == 0) throw ActionException.of("drink.not.thirsty");

		// Drink water
		final int actual = rec.consume(thirst.get());
		thirst.modify(-actual);

		// Build response
		final var response = response(rec);
		if(thirst.get() == 0) {
			response.add("drink.thirst.quenched");
		}
		return response.build();
	}

	/**
	 * Helper - Builds a drink response.
	 */
	private static Response.Builder response(Receptacle rec) {
		final var response = new Response.Builder();
		response.add(new Description("drink.receptacle", rec.descriptor().name()));
		if(rec.level() == 0) {
			response.add(new Description("drink.receptacle.emptied", rec.name()));
		}
		return response;
	}
}
