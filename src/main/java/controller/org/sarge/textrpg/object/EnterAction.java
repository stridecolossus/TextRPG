package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;

/**
 * Enter a {@link Vehicle}.
 * @author Sarge
 */
public class EnterAction extends AbstractAction {
	@Override
	public boolean isVisibleAction() {
		return true;
	}

	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING, Stance.MOUNTED};
	}
	
	/**
	 * Enters a vehicle.
	 * @param ctx
	 * @param actor
	 * @param vehicle
	 * @return
	 * @throws ActionException if the actor cannot enter the vehicle
	 */
	public ActionResponse enter(Entity actor, Vehicle vehicle) throws ActionException {
		actor.setParent(vehicle);
		return response(vehicle);
	}
}
