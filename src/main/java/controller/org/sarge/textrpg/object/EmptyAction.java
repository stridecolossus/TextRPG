package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.entity.Entity;

/**
 * Action to empty something.
 * @author Sarge
 */
public class EmptyAction extends AbstractAction {
	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}
	
	/**
	 * Empty a receptacle.
	 * @param ctx
	 * @param actor
	 * @param rec
	 * @throws ActionException
	 */
	public ActionResponse empty(Entity actor, Receptacle rec) throws ActionException {
		rec.empty();
		return response(rec);
	}

	/**
	 * Empty a container.
	 * @param ctx
	 * @param actor
	 * @param container
	 * @throws ActionException
	 */
	public ActionResponse empty(Entity actor, Container container) throws ActionException {
		empty(container, container.parent());
		return response(container);
	}
		
	/**
	 * Empty a container to another container.
	 * @param ctx
	 * @param actor
	 * @param container
	 * @param dest
	 * @throws ActionException
	 */
	public ActionResponse empty(Entity actor, Container container, Container dest) throws ActionException {
		empty(container, dest);
		return response(container);
	}

	/**
	 * Moves contents to the given parent.
	 */
	private static void empty(Container container, Parent parent) throws ActionException {
		if(container.contents().size() == 0) throw new ActionException("container.empty.empty");
		container.empty(parent);
	}
}
