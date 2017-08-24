package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.Location;

/**
 * Action to throw an object at something.
 * @author Sarge
 */
@SuppressWarnings("unused")
public class ThrowAction extends AbstractAction {
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Discard an object.
	 * @throws ActionException
	 */
	public void throwObject(Entity actor, WorldObject obj) throws ActionException {
		verifyCarried(actor, obj);
		final Location loc = actor.getLocation();
		obj.setParentAncestor(loc);
	}

	/**
	 * Throws at someone.
	 */
	public void throwObjectEntity(Entity actor, WorldObject obj, Thing target) {
		// TODO - ???
	}
}
