package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.Money;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to give an object or money to another entity.
 * @author Sarge
 */
@Component
public class GiveAction extends AbstractAction {
	/**
	 * Gives some coins.
	 * @param actor			Actor
	 * @param money			Money to give
	 * @param entity		Entity to give to
	 * @return Response
	 * @throws ActionException if the actor does not have the specified money or the given entity will not accept
	 */
	@RequiresActor
	public Response give(Entity actor, Money money, Entity entity) throws ActionException {
		/**
		 * TODO
		 * - ok if player
		 * - otherwise check for topic/quest?
		 * - transaction
		 */
		return null;
	}

	/**
	 * Gives an object.
	 * @param actor			Actor
	 * @param obj			Object to give
	 * @param entity		Entity to give to
	 * @return Response
	 * @throws ActionException if the actor does not possess the given object or the entity will not accept it
	 */
	@RequiresActor
	public Response give(Entity actor, @Carried WorldObject obj, Entity entity) throws ActionException {
		// TODO
		return null;
	}
}
