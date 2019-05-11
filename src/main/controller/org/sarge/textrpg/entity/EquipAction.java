package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.Light;
import org.sarge.textrpg.object.LightController;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * Equips or removes a piece of equipment.
 * @see Equipment
 * @author Sarge
 */
// TODO - equip/remove [filter|all]
public class EquipAction extends AbstractAction {
	private final LightController controller;

	/**
	 * Constructor.
	 * @param controller Light controller
	 */
	public EquipAction(LightController controller) {
		super(Flag.OUTSIDE);
		this.controller = notNull(controller);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Equips an object.
	 * @param actor			Actor
	 * @param obj			Object to equip
	 * @return Response
	 * @throws ActionException if the object cannot be equipped
	 */
	public Response equip(Entity actor, @Carried(auto=true) WorldObject obj) throws ActionException {
		// Check can be equipped
		final Slot slot = obj.descriptor().equipment().slot();
		if(slot == Slot.NONE) throw ActionException.of("equip.cannot.equip");

		// Equip
		final Inventory inv = actor.contents();
		final Equipment equipment = inv.equipment();
		if(equipment.contains(obj)) throw new ActionException(new Description("equip.already.equipped", obj.name()));
		if(equipment.contains(slot)) throw new ActionException(describe("equip.slot.occupied", obj, slot));
		equipLocal(actor, obj, slot, equipment, "equip");

		// Build response
		return Response.of(describe("equip.object.response", obj, slot));
	}

	/**
	 * Holds an object.
	 * @param actor		Actor
	 * @param obj		Object to hold
	 * @return Response
	 * @throws ActionException if the actors hands are full
	 */
	public Response hold(Entity actor, @Carried(auto=true) WorldObject obj) throws ActionException {
		final Equipment equipment = actor.contents().equipment();
		final Optional<Slot> slot = equipment.free();
		if(slot.isPresent()) {
			return equipLocal(actor, obj, slot.get(), equipment, "hold");
		}
		else {
			throw ActionException.of("hold.hands.full");
		}
	}

	/**
	 * Removes an equipped object.
	 * @param actor			Actor
	 * @param obj			Object to remove
	 * @return Response
	 * @throws ActionException if the object cannot be removed
	 */
	public Response remove(Entity actor, WorldObject obj) throws ActionException {
		// Check can be equipped
		final Slot slot = obj.descriptor().equipment().slot();
		if(slot == Slot.NONE) throw ActionException.of("remove.cannot.equip");

		// Check is equipped
		final Equipment equipment = actor.contents().equipment();
		if(!equipment.contains(obj)) throw ActionException.of("remove.not.equipped");
		equipment.remove(slot);

		// Remove two-handed from both slots
		if(obj.descriptor().properties().isTwoHanded()) {
			assert slot == Slot.MAIN;
			equipment.remove(Slot.OFF);
		}

		// Restore to inventory or drop
		final InventoryController controller = new InventoryController("remove");
		final var result = controller.take(actor, obj);
		return Response.of(result);
	}

	/**
	 * Removes all equipment.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor has no equipment
	 */
	public Response clear(Entity actor) throws ActionException {
		// Enumerate equipment
		final Equipment equipment = actor.contents().equipment();
		final var objects = equipment.equipment().values().stream().collect(toList());

		// Remove all equipment
		equipment.clear();

		// Restore to inventory
		final InventoryController controller = new InventoryController("remove");
		final var results = controller.take(actor, objects.stream());

		// Build response
		return Response.of(results);
	}

	/**
	 * Helper - Equips an object.
	 * @param actor			Actor
	 * @param obj			Object
	 * @param slot			Slot
	 * @param equipment		Equipment model
	 * @return Response
	 * @throws ActionException if the object cannot be equipped
	 */
	private Response equipLocal(Entity actor, WorldObject obj, Slot slot, Equipment equipment, String prefix) throws ActionException {
		// Check two-handed objects
		final boolean two = obj.descriptor().properties().isTwoHanded();
		if(two) {
			assert slot == Slot.MAIN;
			if(!equipment.isTwoHandedAvailable()) throw ActionException.of("equip.invalid.twohanded");
		}

		// Equip object
		equipment.equip(obj, slot);
		if(two) {
			equipment.equip(obj, Slot.OFF);
		}

		// Build action response
		final Response.Builder builder = new Response.Builder();
		final String key = TextHelper.join(prefix, "object.response");
		builder.add(describe(key, obj, slot));

		// Auto-light
		if(obj instanceof Light) {
			final Light light = (Light) obj;
			if(!light.isActive()) {
				try {
					controller.light(actor, light);
					builder.add(new Description("action.light.light", light.name()));
				}
				catch(ActionException e) {
					// Ignored
				}
			}
		}

		// Build response
		return builder.build();
	}

	/**
	 * Helper - Builds a description including arguments for the slot verb and placement.
	 * @param key		Description key
	 * @param obj		Object
	 * @param slot		Deployment slot
	 * @return Description
	 */
	private static Description describe(String key, WorldObject obj, Slot slot) {
		return new Description.Builder(key)
			.name(obj.name())
			.add("verb", TextHelper.join("slot.verb", slot.verb()))
			.add("place", TextHelper.join("slot.place", slot.placement()))
			.build();
	}
}
