package org.sarge.textrpg.entity;

import java.util.Collections;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.DeploymentSlot;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

/**
 * Action to equip an object.
 * @author Sarge
 */
public class EquipAction extends AbstractAction {
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}

	/**
	 * Equip object.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @throws ActionException
	 */
	public ActionResponse equip(ActionContext ctx, Entity actor, WorldObject obj) throws ActionException {
		return new ActionResponse(equip(actor, obj));
	}
	
	/**
	 * Helper.
	 */
	protected static Description equip(Entity actor, WorldObject obj) throws ActionException {
		// Check carried
		if(obj.getOwner() != actor) throw new ActionException("equip.not.carried");

		// Check object can be equipped
		final ObjectDescriptor.Equipment equipment = obj.getDescriptor().getEquipment();
		if(equipment.getDeploymentSlot().map(slot -> slot == DeploymentSlot.MAIN_HAND).orElse(false)) throw new ActionException("equip.main.hand");
		if(!equipment.getCondition().evaluate(actor)) throw new ActionException("equip.cannot.equip");
		
		// Equip
		actor.getEquipment().equip(obj);
		actor.modify(EntityValue.ARMOUR, obj.getDescriptor().getEquipment().getArmour());
		
		// Apply passive effects
		final Effect.Descriptor passive = equipment.getPassive();
		passive.apply(Collections.singletonList(actor), actor);
		
		// Build response
		final DeploymentSlot slot = equipment.getDeploymentSlot().get();
		return new Description.Builder("equip.response")
			.wrap("verb", slot.getVerb())
			.wrap("name", obj)
			.wrap("place", slot.getPlacement())
			.wrap("slot", "slot." + slot)
			.build();
	}
}
