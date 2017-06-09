package org.sarge.textrpg.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.Light;
import org.sarge.textrpg.object.Light.Operation;
import org.sarge.textrpg.object.LightAction;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.object.WorldObject;

/**
 * Action to hold something.
 * @author Sarge
 * @see Equipment#hold(WorldObject)
 */
public class HoldAction extends AbstractAction {
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}
	
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}
	
	/**
	 * Holds or wields an object.
	 * @param ctx		Context
	 * @param actor		Actor
	 * @param obj		Object to hold
	 * @return Response
	 * @throws ActionException if both hands are full
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, WorldObject obj) throws ActionException {
		verifyCarried(actor, obj);
		if(obj.getDescriptor() instanceof Weapon) {
			// Stop holding current object
			final List<Description> responses = new ArrayList<>();
			final Optional<WorldObject> held = actor.getEquipment().removeWielded();
			held.ifPresent(prev -> responses.add(new Description("remove.response", "name", prev)));
			
			// Delegate
			final Description desc = EquipAction.equip(actor, obj);
			responses.add(desc);
			return new ActionResponse(responses);
		}
		else {
			// Hold object
			actor.getEquipment().hold(obj);
			return response(obj);
		}
	}
	
	/**
	 * Holds a light.
	 * @param ctx		Context
	 * @param actor		Actor
	 * @param obj		Object to hold
	 * @return Response
	 * @throws ActionException if both hands are full
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, Light light) throws ActionException {
		// Hold light
		verifyCarried(actor, light);
		final List<Description> responses = new ArrayList<>();
		actor.getEquipment().hold(light);
		responses.add(new Description(this.name + ".response", "name", light));

		// Turn on light
		if(!light.isLit()) {
			try {
				light.execute(Operation.LIGHT, ctx.getTime());
				responses.add(LightAction.buildResponse(light, Operation.LIGHT));
			}
			catch(ActionException e) {
				// Ignored
			}
		}
		
		// Build response
		return new ActionResponse(responses);
	}
}
