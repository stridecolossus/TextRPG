package org.sarge.textrpg.entity;

import java.util.Collections;

import org.sarge.textrpg.common.AbstractAction;
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
	 * @param actor
	 * @param obj
	 * @throws ActionException
	 */
	public ActionResponse equip(Entity actor, WorldObject obj) throws ActionException {
		return new ActionResponse(equipObject(actor, obj));
	}
	
	protected static Description equipObject(Entity actor, WorldObject obj) throws ActionException {
		// Check carried
		if(obj.getOwner() != actor) throw new ActionException("equip.not.carried");

		// Check object can be equipped
		// TODO - duplicated in equip()
		final ObjectDescriptor.Equipment equipment = obj.getDescriptor().getEquipment().orElseThrow(() -> new ActionException("equipment.cannot.equip"));
		// TODO - ???
		//final ObjectDescriptor.Equipment equipment = obj.getDescriptor().getEquipment();
		//if(equipment.getDeploymentSlot().map(slot -> slot == DeploymentSlot.MAIN_HAND).orElse(false)) throw new ActionException("equip.main.hand");
		//if(!equipment.getCondition().evaluate(actor)) throw new ActionException("equip.cannot.equip");

		// Equip
		actor.getEquipment().equip(obj);

		// Apply passive effects
		final Effect.Descriptor passive = equipment.getPassive();
		passive.apply(Collections.singletonList(actor), actor);
		actor.modify(EntityValue.ARMOUR, equipment.getArmour());

		// Build response
		final DeploymentSlot slot = equipment.getDeploymentSlot();
		return new Description.Builder("equip.response")
			.wrap("verb", "equip." + slot.getVerb())					// TODO - dup prefix
			.wrap("name", obj)
			.wrap("place", "equip.verb." + slot.getPlacement())			// TODO - dup prefix
			.wrap("slot", "slot." + slot)
			.build();
	}
}
