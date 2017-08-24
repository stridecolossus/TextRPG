package org.sarge.textrpg.world;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.entity.Corpse;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.WorldObject;

/**
 * Dig up or bury something.
 * @author Sarge
 */
public class DigAction extends AbstractAction {
	/**
	 * Buries an object.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @return
	 */
	public ActionResponse bury(Entity actor, WorldObject obj) {
		// TODO
		return null;
	}

	/**
	 * Buries a corpse.
	 * @param ctx
	 * @param actor
	 * @param corpse
	 * @return
	 */
	public ActionResponse bury(Entity actor, Corpse corpse) {
		// TODO
		return null;
	}
	
	/**
	 * Digs something up in this location.
	 * @param ctx
	 * @param actor
	 * @return
	 */
	public ActionResponse dig(Entity actor) {
		// TODO
		return null;
	}
}
