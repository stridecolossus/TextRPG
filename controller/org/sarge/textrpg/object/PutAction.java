package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Link;
import org.sarge.textrpg.world.ReverseLink;

/**
 * Action to put an object into a {@link Container} or an {@link ContainerLink}.
 * @author Sarge
 */
@SuppressWarnings("unused")
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
	public ActionResponse execute(ActionContext ctx, Entity actor, WorldObject obj, Container c) throws ActionException {
		// Move object to container
		verifyCarried(actor, obj);
		obj.take(actor);
		obj.setParent(c);
		
		// Build response
		final Description desc = new Description.Builder("put.response")
			.wrap("name", obj)
			.wrap("place", "place." + c.getDescriptor().getPlacement())
			.wrap("container", c)
			.build();
		return new ActionResponse(desc);
	}
	
	/**
	 * Put object into an object-link.
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, WorldObject obj, Hidden hidden) throws ActionException {
		// Find object link
		final Link result = actor.getLocation().getExits().values().stream()
			.map(Exit::getLink)
			.filter(link -> link.getController().map(c -> c == hidden).orElse(false))
			.findFirst().orElseThrow(() -> new ActionException(ILLOGICAL));

		// Convert
		final ContainerLink link;
		if(result instanceof ReverseLink) {
			final ReverseLink reverse = (ReverseLink) result;
			link = (ContainerLink) reverse.getLink();
		}
		else {
			link = (ContainerLink) result;
		}
		
		// Put object in link
		verifyCarried(actor, obj);
		link.put(obj);
		obj.hide();
		//obj.setParentAncestor(actor.getLocation());
			
		// Build response
		final Description desc = new Description.Builder("put.link.response")
			.wrap("name", obj)
			.wrap("link", link.getController().get())
			.build();
		return new ActionResponse(desc);
	}
}
