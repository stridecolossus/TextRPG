package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.RevealNotification;
import org.sarge.textrpg.entity.ActionHelper;
import org.sarge.textrpg.entity.Entity;

/**
 * Action to examine a location, object or entity.
 * @author Sarge
 */
public class ExamineAction extends AbstractAction {
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Reveals a hidden object.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse examine(ActionContext ctx, Entity actor, RevealObject obj) throws ActionException {
		// Reveal hidden object
		if(!obj.isRevealed()) {
			final WorldObject revealed = obj.reveal();
			final Notification n = new RevealNotification("revealed.object", revealed);
			actor.getNotificationHandler().handle(n);
		}

		// Delegate
		return examine(actor, obj);
	}
	
	/**
	 * Examines an object.
	 */
	public ActionResponse examine(ActionContext ctx, Entity actor, WorldObject obj) throws ActionException {
		return examine(actor, obj);
	}
	
	/**
	 * Examines an object.
	 */
	private static ActionResponse examine(Entity actor, WorldObject obj) {
		final Description desc = ActionHelper.describe(actor, obj);
		return new ActionResponse(desc);
	}

	/**
	 * Examines an entity.
	 * @param ctx
	 * @param actor
	 * @param entity
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse examine(ActionContext ctx, Entity actor, Entity entity) throws ActionException {
		return new ActionResponse(entity.describe());
	}

	/**
	 * Examines a decoration.
	 * @param ctx
	 * @param actor
	 * @param decoration
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse examine(ActionContext ctx, Entity actor, String decoration) throws ActionException {
		return new ActionResponse("examine.decoration");
	}
}
