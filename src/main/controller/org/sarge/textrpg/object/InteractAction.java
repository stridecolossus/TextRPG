package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.EnumAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.WorldObject.Interaction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;

/**
 * Action to interact with a {@link Control}.
 * @author Sarge
 */
@EnumAction(Interaction.class)
public class InteractAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public InteractAction() {
		super(Flag.BROADCAST);
	}

	/**
	 * Interacts with the given control.
	 * @param actor				Actor
	 * @param interaction		Interaction
	 * @param control			Control
	 * @return Response
	 * @throws ActionException if this interaction is invalid for the given control
	 */
	@RequiresActor
	@ActionOrder(3)
	public Response interact(Entity actor, Interaction interaction, Control control) throws ActionException {
		assert interaction != Interaction.EXAMINE;
		final Description response = control.interact(actor, interaction);
		return Response.of(response);
	}

	/**
	 * Interacts with an object that does nothing.
	 * @param interaction		Interaction
	 * @param obj				Basic object
	 * @return Nothing
	 * @throws ActionException {@link Control#IGNORED}
	 */
	public void interact(Interaction interaction, WorldObject obj) throws ActionException {
		throw Control.IGNORED;
	}
}
