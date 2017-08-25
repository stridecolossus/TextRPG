package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Rope.Anchor;

/**
 * Action to manipulate a {@link Rope}.
 * @author Sarge
 */
public class RopeAction extends AbstractAction {
	/**
	 * Attach rope.
	 * @param ctx
	 * @param actor
	 * @param rope
	 * @param anchor
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse attach(Entity actor, Rope rope, Anchor anchor) throws ActionException {
		verifyCarried(actor, rope);
		rope.attach(actor, anchor);
		return response("rope.attach.response", rope, anchor);
	}

	/**
	 * Remove rope.
	 * @param ctx
	 * @param actor
	 * @param rope
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse remove(Entity actor, Rope rope) throws ActionException {
		rope.remove(actor);
        final Anchor anchor = rope.getAnchor().get();
		return response("rope.remove.response", rope, anchor);
	}

	/**
	 * Builds response.
	 * @param key		Key
	 * @param rope		Rope
	 * @param anchor	Anchor
	 * @return Response
	 */
	private static ActionResponse response(String key, Rope rope, Anchor anchor) {
		final Description desc = new Description.Builder(key)
			.wrap("name", rope)
			.wrap("anchor", anchor)
			.build();
		return new ActionResponse(desc);
	}
}
