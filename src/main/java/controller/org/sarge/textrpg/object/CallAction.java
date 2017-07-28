package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.ActionHelper;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.Location;

/**
 * Action to call for a friendly gate to be opened.
 * @author Sarge
 */
public class CallAction extends AbstractAction {
	@Override
	public boolean isVisibleAction() {
		return true;
	}

	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	/**
	 * Call for a gate to be opened or closed.
	 * @param ctx
	 * @param actor
	 * @throws ActionException
	 */
	public ActionResponse call(ActionContext ctx, Entity actor) throws ActionException {
		// Find gate
		final Gate gate = (Gate) actor.getLocation().getExits().values().stream()
			.map(exit -> exit.getLink().getController())
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(obj -> obj instanceof Gate)
			.findFirst()
			.orElseThrow(() -> new ActionException("call.invalid.location"));

		// Call
		final boolean open = gate.getOpenableModel().get().isOpen();
		gate.call();
		// TODO - check friendly

		// Register reset event
		ActionHelper.registerOpenableEvent(actor.getLocation(), (Location) gate.getDestination(), gate, "gate.close");

		// Build response
		final Description desc = new Description.Builder("call.response")
			.wrap("name", gate)
			.wrap("state", "call." + (open ? "closed" : "opened"))
			.build();
		return new ActionResponse(desc);
	}
}
