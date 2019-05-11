package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.springframework.stereotype.Component;

/**
 * Action to eat something.
 * @author Sarge
 */
@Component
public class ConsumeAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public ConsumeAction() {
		super(Flag.OUTSIDE);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Consumes the given food.
	 * @param actor		Actor
	 * @param food		Food
	 * @return Response
	 * @throws ActionException
	 */
	@RequiresActor
	public Response consume(Entity actor, @Carried Food food) throws ActionException {
		// Check actor is hungry
		final MutableIntegerMap.MutableEntry hunger = actor.model().values().get(EntityValue.HUNGER.key());
		if(hunger.get() == 0) throw ActionException.of("consume.not.hungry");

		// Reduce hunger
		final int nutrition = food.descriptor().nutrition();
		final int actual = Math.min(nutrition, hunger.get());
		final int result = hunger.modify(-actual);

		// Consume food
		food.destroy();

		// Build response
		final Response.Builder response = new Response.Builder();
		response.add(new Description("action.consume.response", food.name()));
		if(result == 0) {
			response.add("consume.hunger.zero");
		}
		return response.build();
	}
}
