package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.Location;

/**
 * Action to drop an object.
 * @author Sarge
 */
public class DropAction extends AbstractAction {
	@Override
	public boolean isCombatBlockedAction() {
		return false;
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
	 * Drops an object from the inventory.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, WorldObject obj) throws ActionException {
		// Check owned by the actor
		verifyCarried(actor, obj);
		
		// Drop to current location
		final Location loc = actor.getLocation().getBase();
		obj.setParentAncestor(loc);
		
		// Build response
		return response(obj);
	}
}
