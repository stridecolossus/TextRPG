package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.springframework.stereotype.Component;

/**
 * Action to attach a rope to an anchor.
 * @author Sarge
 * TODO - rope links have a length? otherwise whats the point of rope having a length?
 */
@Component
public class RopeAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public RopeAction() {
		super(Flag.LIGHT, Flag.BROADCAST);
	}

	/**
	 * Attaches the given rope to an anchor.
	 * @param rope			Rope
	 * @param anchor		Anchor
	 * @return Response
	 * @throws ActionException if the rope is broken, is already attached to another anchor, or the given anchor is occupied
	 */
	@RequiresActor
	public Response attach(Entity actor, @Carried(auto=true) Rope rope, Rope.Anchor anchor) throws ActionException {
		rope.attach(anchor);
		rope.parent(actor.location());
		return Response.OK;
	}

	/**
	 * Removes the given rope from its anchor and restores to the actors inventory.
	 * @param actor			Actor
	 * @param rope			Rope to remove
	 * @return Response
	 * @throws ActionException if the rope is not attached to an anchor or is not in the current location
	 */
	@RequiresActor
	@ActionOrder(2)
	public Response detach(Entity actor, Rope rope) throws ActionException {
		// Remove from anchor
		if(rope.parent() != actor.parent()) throw ActionException.of("rope.remove.invalid");
		rope.remove();

		// Restore to inventory
		final InventoryController controller = new InventoryController("rope.remove");
		final var result = controller.take(actor, rope);

		// Build response
		return Response.of(result);
	}

	/**
	 * Pulls an attached rope.
	 * @param actor			Actor
	 * @param rope			Rope to pull
	 * @return Response
	 * @throws ActionException if the actor is not at the bottom end of the rope or it is not attached to an anchor
	 */
	@RequiresActor
	public Response pull(Entity actor, Rope rope) throws ActionException {
		// Check at bottom end of an attached rope
		if(rope.parent() == actor.parent()) throw ActionException.of("rope.pull.invalid");

		// Pull rope
		if(rope.descriptor().isMagical()) {
			rope.remove();
			final InventoryController controller = new InventoryController("rope.pull");
			final var result = controller.take(actor, rope);
			return Response.of(result);
		}
		else {
			return Response.of("rope.pull.nothing");
		}
	}

	/**
	 * Measures a rope.
	 * @param rope Rope to measure
	 * @return Response
	 * @throws ActionException if the rope is attached to something
	 */
	public Response measure(@Carried Rope rope) throws ActionException {
		if(rope.anchor() != null) throw ActionException.of("rope.measure.attached");
		return Response.of(new Description.Builder("rope.measure.length").name(rope.name()).add("length", rope.descriptor().length()).build());
	}
}
