package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.DeploymentSlot;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

/**
 * Equipment worn by an {@link Entity}.
 * TODO - factor out some of the code to make the higher-level methods simpler to read
 * @author Sarge
 */
public class Equipment {
	private final Map<DeploymentSlot, WorldObject> equipment = new HashMap<>();
	private final Map<DeploymentSlot, Container> containers = new HashMap<>();

	private int weight;

	/**
	 * Looks up an equipped object.
	 * @param slot Deployment slot
	 * @return Equipped gear
	 */
	public Optional<WorldObject> get(DeploymentSlot slot) {
		return Optional.ofNullable(equipment.get(slot));
	}

	/**
	 * @return Equipped gear
	 */
	public Stream<WorldObject> stream() {
		return equipment.values().stream();
	}

	/**
	 * @return Total weight of equipment
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Describes this set of equipment.
	 * @return Description
	 */
	public List<Description> describe() {
		final Function<DeploymentSlot, Description> mapper = slot -> {
			// Determine how to render this slot
			final String key;
			switch(slot) {
			case MAIN_HAND:
			case OFF_HAND:
				key = "info.equipment.entry.hand";
				break;

			default:
				key = "info.equipment.entry";
				break;
			}

			// Build slot description
			final WorldObject obj = equipment.get(slot);
			final Description.Builder builder = new Description.Builder(key)
				.wrap("place", "equip.verb." + slot.getPlacement())		// TODO - duplicate prefix
				.wrap("slot", "slot." + slot)
				.wrap("name", obj.getName());

			// TODO - durable wear

			return builder.build();
		};
		return equipment.keySet().stream().map(mapper).collect(toList());
	}

	/**
	 * Holds an object.
	 * @param obj Object to hold
	 * @throws ActionException if both hands are full
	 */
	public void hold(WorldObject obj) throws ActionException {
		// Find empty hand
		final DeploymentSlot hand;
		if(equipment.get(DeploymentSlot.MAIN_HAND) == null) {
			final boolean main = obj.getDescriptor().getEquipment().map(ObjectDescriptor.Equipment::getDeploymentSlot).map(slot -> slot == DeploymentSlot.MAIN_HAND).isPresent();
			if(main) {
				// Delegate if actually equipped as weapon
				equip(obj);
				return;
			}
			else {
				// Otherwise just hold
				hand = DeploymentSlot.MAIN_HAND;
			}
		}
		else {
			// Use other hand
			if(equipment.get(DeploymentSlot.OFF_HAND) != null) throw new ActionException("hold.hands.full");
			hand = DeploymentSlot.OFF_HAND;
		}

		// Hold object
		equipment.put(hand, obj);
		add(obj);
	}

	/**
	 * Removes the object in equipped in {@link DeploymentSlot#MAIN_HAND}.
	 */
	public Optional<WorldObject> removeWielded() {
		final WorldObject obj = equipment.get(DeploymentSlot.MAIN_HAND);
		if(obj != null) removeObject(obj);
		return Optional.ofNullable(obj);
	}

	/**
	 * Equips the given object.
	 * @param obj Object to equip
	 * @return Whether the object was equipped to a container
	 * @throws ActionException if the slot is occupied
	 * @throws IllegalArgumentException if the object cannot be equipped
	 */
	protected boolean equip(WorldObject obj) throws ActionException {
		final ObjectDescriptor.Equipment desc = obj.getDescriptor().getEquipment().orElseThrow(() -> new ActionException("equipment.cannot.equip"));
		final DeploymentSlot slot = desc.getDeploymentSlot();
		if(slot.isContainerSlot()) {
			// Add object to container
			final Container c = containers.get(slot);
			if(c == null) throw new ActionException("equipment.equip.missing");
			obj.setParent(c);
			add(obj);
			return true;
		}
		else {
			// Check slot available
			if(equipment.containsKey(slot)) throw new ActionException("equipment.equip.occupied");

			// Check two-handed equipment
			switch(slot) {
			case MAIN_HAND:
			case OFF_HAND:
				final DeploymentSlot other = slot == DeploymentSlot.MAIN_HAND ? DeploymentSlot.OFF_HAND : DeploymentSlot.MAIN_HAND;
				if(desc.isTwoHanded()) {
					if(equipment.containsKey(other)) throw new ActionException("equipment.two.handed");
				}
				else {
					final WorldObject otherObject = equipment.get(other);
					if(otherObject != null) {
						if(otherObject.getDescriptor().getEquipment().get().isTwoHanded()) throw new ActionException("equipment.two.handed");
					}
				}
				break;
			}

			// Equip object to the slot
			equipment.put(slot, obj);
			add(obj);

			// Register equipment containers
			if(obj instanceof Container) {
				final Container c = (Container) obj;
				final Optional<DeploymentSlot> contentSlot = c.getDescriptor().getContentsDeploymentSlot();
				contentSlot.ifPresent(cs -> containers.put(cs, c));
			}

			return false;
		}
	}

	/**
	 * Updates weight.
	 */
	private void add(WorldObject obj) {
		weight += obj.getDescriptor().getProperties().getWeight();
	}

	/**
	 * Removes the equipped object in the given slot.
	 * @param slot Deployment slot
	 * @throws ActionException if the object is not equipped
	 * @throws IllegalArgumentException if the object cannot be equipped
	 */
	protected void remove(WorldObject obj) throws ActionException {
		final ObjectDescriptor.Equipment desc = obj.getDescriptor().getEquipment().orElseThrow(() -> new ActionException("equipment.not.equipped"));
		final DeploymentSlot slot = desc.getDeploymentSlot();
		if(slot.isContainerSlot()) {
			// Un-equip container contents
			final Container c = containers.get(slot);
			if(c == null) throw new ActionException("equipment.remove.missing");
			// TODO
			// c.empty();
			removeObject(c);
		}
		else {
			// Un-equip object
			if(!equipment.containsKey(slot)) throw new ActionException("equipment.remove.empty");
			equipment.remove(slot);

			// Remove registered containers
			if(obj instanceof Container) {
				final Container c = (Container) obj;
				final Optional<DeploymentSlot> contentSlot = c.getDescriptor().getContentsDeploymentSlot();
				contentSlot.ifPresent(cs -> containers.remove(cs, c));
			}

			// Update stats
			removeObject(obj);
		}
	}

	/**
	 * Updates weight.
	 */
	private void removeObject(WorldObject obj) {
		weight -= obj.getWeight();
		assert weight >= 0;
	}

	/**
	 * Removes all equipment.
	 * @throws ActionException if nothing is equipped
	 */
	void removeAll() throws ActionException {
		if(equipment.isEmpty()) throw new ActionException("equipment.remove.all");
		equipment.clear();
		containers.clear();
		weight = 0;
	}

	@Override
	public String toString() {
		return equipment.toString();
	}
}
