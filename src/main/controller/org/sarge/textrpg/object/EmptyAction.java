package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to empty a container or receptacle.
 * @author Sarge
 */
@Component
public class EmptyAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public EmptyAction() {
		super(Flag.BROADCAST);
	}

	/**
	 * Empties a container.
	 * @param actor			Actor
	 * @param container		Container
	 * @return Response
	 * @throws ActionException if the container is closed or empty
	 */
	@RequiresActor
	public Response empty(Entity actor, Container container) throws ActionException {
		// Check container is open
		if(!container.isOpen()) throw ActionException.of("empty.container.closed");

		// Check contents can be moved
		final Contents contents = container.contents();
		if(contents.isEmpty()) throw ActionException.of("empty.container.empty");
		if(!contents.isRemoveAllowed()) throw ActionException.of("empty.container.immutable");

		// Empty contents to location
		contents.move(actor.location());

		return Response.OK;
	}

	/**
	 * Empties a receptacle.
	 * @param rec Receptacle
	 * @return Response
	 * @throws ActionException if the receptacle is infinite or already empty
	 */
	public Response empty(Receptacle rec) throws ActionException {
		rec.empty();
		return Response.OK;
	}
}
