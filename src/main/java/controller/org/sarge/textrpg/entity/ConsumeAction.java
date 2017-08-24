package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.Food;
import org.sarge.textrpg.util.IntegerMap;

/**
 * Action to eat some {@link Food}.
 * @author Sarge
 */
public class ConsumeAction extends AbstractAction {
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Consumes some food.
	 * @param ctx
	 * @param actor
	 * @param food
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse consume(Entity actor, Food food) throws ActionException {
		// Check carried
		verifyCarried(actor, food);
		
		// Check whether hungry
		final IntegerMap<EntityValue> values = actor.getValues();
		if(values.get(EntityValue.HUNGER) == 0) throw new ActionException("consume.not.hungry");
		
		// Consume food
		food.consume();

		// Reduce hunger
		final int value = food.getDescriptor().getLevel();
		actor.modify(EntityValue.HUNGER, -value);
		
		// Build response
		final Description.Builder desc = new Description.Builder("consume.response").add("name", food);
		if(values.get(EntityValue.HUNGER) <= 0) {
			desc.add(new Description("consume.full"));
		}
		return new ActionResponse(desc.build());
	}
}
