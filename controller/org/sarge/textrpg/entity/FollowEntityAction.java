package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;

/**
 * Action to follow an {@link Entity}.
 * @author Sarge
 */
public class FollowEntityAction extends AbstractAction {
	public ActionResponse execute(ActionContext ctx, Entity actor, Entity entity) throws ActionException {
		actor.follow(entity);
		return response(actor);
	}
	
	@Override
	public ActionResponse execute(ActionContext ctx, Entity actor) throws ActionException {
		actor.follow(null);
		return response(actor);
	}
}
