package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.DeploymentSlot;
import org.sarge.textrpg.object.Light;
import org.sarge.textrpg.object.Light.Operation;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectFilter;
import org.sarge.textrpg.object.WorldObject;

/**
 * Action to remove an equipped object.
 * @author Sarge
 */
public class RemoveAction extends AbstractAction {
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	/**
	 * Remove an equipped object.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, WorldObject obj) throws ActionException {
		return new ActionResponse(remove(actor, obj, ctx.getTime()));
	}

	private static List<Description> remove(Entity actor, WorldObject obj, long time) throws ActionException {
		// Remove equipment
		actor.getEquipment().remove(obj);

		// Snuff lights
		final List<Description> responses = new ArrayList<>();
		if(obj instanceof Light) {
			final Light light = (Light) obj;
			if(light.isLit()) {
				light.execute(Operation.SNUFF, time);
			}
		}

		// Remove passive effects
		final ObjectDescriptor.Equipment equipment = obj.getDescriptor().getEquipment().get();
		final Effect.Descriptor effect = equipment.getPassive();
		// TODO - how to invert? also need to check all values are literals otherwise needs to use AppliedEffect?
		// effect.apply(Collections.singletonList(e), actor, ctx.getEventQueue());
		actor.modify(EntityValue.ARMOUR, -equipment.getArmour());

		// Build response
		final DeploymentSlot slot = equipment.getDeploymentSlot();
		final Description desc = new Description.Builder("remove.response")
			.wrap("verb", "equip." + slot.getVerb() + ".stop")
			.wrap("name", obj)
			.build();
		responses.add(desc);
		return responses;
	}

	/**
	 * Remove specified equipment.
	 * @param ctx
	 * @param actor
	 * @param filter
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, ObjectFilter filter) throws ActionException {
		final List<WorldObject> list = actor.getEquipment().stream().filter(obj -> filter.test(obj.getDescriptor())).collect(toList());
		if(list.isEmpty()) {
			throw new ActionException("remove.empty.filter");
		}
		else {
			final List<Description> responses = new ArrayList<>();
			for(final WorldObject obj : list) {
				try {
					responses.addAll(remove(actor, obj, ctx.getTime()));
				}
				catch(final ActionException e) {
					responses.add(new Description(e.getMessage()));
				}
			}
			return new ActionResponse(responses);
		}
	}
}
