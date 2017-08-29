package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractActiveAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.object.WorldObject.Interaction;

/**
 * Manipulate an object.
 * @author Sarge
 */
public class InteractAction extends AbstractActiveAction {
	private final Interaction action;
	private final int mod;

	/**
	 * Constructor.
	 * @param action		Interaction
	 * @param mod			Stamina multiplier
	 */
	public InteractAction(Interaction action, int mod) {
		super(action.name());
		Check.zeroOrMore(mod);
		this.action = action;
		this.mod = mod;
	}

	@Override
	public boolean isCombatBlockedAction() {
		return false;
	}

	@Override
	public boolean isVisibleAction() {
		return true;
	}

	/**
	 * Use a control.
	 * @param actor
	 * @param control
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(Entity actor, Control control) throws ActionException {
		control.apply(action, actor);
		return build(control);
	}

	/**
	 * Pulls a rope.
	 * @param actor
	 * @param rope
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(Entity actor, Rope rope) throws ActionException {
		if(action == Interaction.PULL) {
			rope.pull(actor);
			return response("rope.pull");
		}
		else {
			return nothing();
		}
	}

	/**
	 * Interact with an object.
	 * @param actor
	 * @param obj
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(Entity actor, InteractObject obj) throws ActionException {
		// Check sufficient strength
		final int str = obj.getRequiredStrength();
		if(actor.getAttributes().get(Attribute.STRENGTH) < str) throw new ActionException("interact.insufficient.strength");

		// Check required stamina
		final int cost = str * mod;
		if(actor.getValues().get(EntityValue.STAMINA) < cost) throw new ActionException("interact.insufficient.stamina");

		// Interact
		obj.interact(action, actor.getLocation());
		actor.modify(EntityValue.STAMINA, -cost);

		// Build response
		return build(obj);
	}

	/**
	 * Interact with an object (that does nothing).
	 * @param actor
	 * @param obj
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse execute(Entity actor, WorldObject obj) throws ActionException {
		return nothing();
	}

	/**
	 * @return Nothing happens response
	 */
	private static ActionResponse nothing() {
		return new ActionResponse("interact.nothing");
	}

	/**
	 * Builds the response.
	 * @param obj Object
	 * @return Response
	 */
	private ActionResponse build(WorldObject obj) {
		final Description res = new Description.Builder("interact.response")
			.wrap("name", obj)
			.wrap("op", "action." + action.name())
			.build();
		return new ActionResponse(res);
	}
}
