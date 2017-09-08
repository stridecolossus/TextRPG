package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.Exit;

/**
 * Action to put an object into a {@link Container} or an {@link ContainerLink}.
 * @author Sarge
 */
public class PutAction extends AbstractAction {
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Put object into container.
	 */
	public ActionResponse execute(Entity actor, WorldObject obj, Container c) throws ActionException {
		// Move object to container
		verifyCarried(actor, obj);
		obj.take(actor);
		obj.setParent(c);

		// Build response
		final Description desc = new Description.Builder("put.response")
			.wrap("name", obj)
			.wrap("place", "place." + c.descriptor().placement())
			.wrap("container", c)
			.build();
		return new ActionResponse(desc);
	}

	/**
	 * Put object into an object-link.
	 */
	public ActionResponse execute(Entity actor, WorldObject obj, Hidden hidden) throws ActionException {
		// Find object link
		final ContainerLink container = actor.location().getExits().values().stream()
			.map(Exit::getLink)
			.filter(link -> link instanceof ContainerLink)
			.filter(link -> link.controller().map(c -> c == hidden).orElse(false))
			.map(link -> (ContainerLink) link)
			.findFirst()
			.orElseThrow(() -> new ActionException(ILLOGICAL));

		// Put object in link
		verifyCarried(actor, obj);
		container.put(obj);
		obj.hide();
		//obj.setParentAncestor(actor.getLocation());

		// Build response
		final Description desc = new Description.Builder("put.link.response")
			.wrap("name", obj)
			.wrap("link", container.controller().get())
			.build();
		return new ActionResponse(desc);
	}
}
