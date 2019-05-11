package org.sarge.textrpg.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Percentile;

/**
 * Model for equipment.
 * @author Sarge
 * TODO - this is only really used for players and characters? could just be basic map for NPCs? actually map/emissions not even needed? could just be Entity.weapon()?
 */
public class Equipment extends AbstractEqualsObject {
	private final Map<Slot, WorldObject> equipment = new HashMap<>();
	private final Map<Emission, Percentile> emissions = new HashMap<>();
	private Optional<Weapon> weapon = Optional.empty();

	/**
	 * @return Equipment as a map indexed by deployment slot
	 */
	public Map<Slot, WorldObject> equipment() {
		return Map.copyOf(equipment);
	}

	/**
	 * Selects equipped objects of the given type.
	 * @param type Object type
	 * @return Matching objects
	 */
	public <T extends WorldObject> Stream<T> select(Class<T> type) {
		return StreamUtil.select(type, equipment.values().stream());
	}

	/**
	 * @param obj Object
	 * @return Whether the given object is equipped
	 */
	public boolean contains(WorldObject obj) {
		return equipment.containsValue(obj);
	}

	/**
	 * @param slot Deployment slot
	 * @return Whether the given slot is occupied
	 */
	public boolean contains(Slot slot) {
		return equipment.containsKey(slot);
	}

	/**
	 * Finds the deployment slot for the given object.
	 * @param obj Object
	 * @return Deployment slot or <tt>null</tt> if not equipped
	 */
	public Slot slot(WorldObject obj) {
		for(Slot slot : equipment.keySet()) {
			if(equipment.get(slot) == obj) return slot;
		}
		return null;
	}

	/**
	 * @return Currently wielded weapon
	 */
	public Optional<Weapon> weapon() {
		return weapon;
	}

	/**
	 * Finds an available hand slot.
	 * @return Free hand slot
	 */
	public Optional<Slot> free() {
		if(equipment.containsKey(Slot.MAIN)) {
			if(equipment.containsKey(Slot.OFF)) {
				return Optional.empty();
			}
			else {
				return Optional.of(Slot.OFF);
			}
		}
		else {
			return Optional.of(Slot.MAIN);
		}
	}

	/**
	 * @return Whether a two-handed object can be equipped (i.e. both hands are available)
	 */
	public boolean isTwoHandedAvailable() {
		if(equipment.containsKey(Slot.MAIN)) return false;
		if(equipment.containsKey(Slot.OFF)) return false;
		return true;
	}

	/**
	 * Equips an object to the given slot.
	 * @param obj		Object to equip
	 * @param slot		Slot
	 * @throws IllegalStateException if the slot is already occupied
	 */
	public void equip(WorldObject obj, Slot slot) {
		if(equipment.containsKey(slot)) throw new IllegalStateException("Slot is already occupied: " + slot);
		equipment.put(slot, obj);
		update();
	}

	/**
	 * Removes the equipped object in the given slot.
	 * @param slot Deployment slot
	 * @throws IllegalStateException if the slot is not occupied
	 */
	public void remove(Slot slot) {
		if(!equipment.containsKey(slot)) throw new IllegalStateException("Slot not occupied: " + slot);
		equipment.remove(slot);
		update();
	}

	/**
	 * Removes <b>all</b> equipment.
	 * @throws IllegalStateException if nothing is equipped
	 */
	public void clear() {
		if(equipment.isEmpty()) throw new IllegalStateException("Equipment already empty");
		equipment.clear();
		emissions.clear();
		weapon = Optional.empty();
	}

	/**
	 * Looks up the highest intensity of the given emission in this equipment.
	 * @param emission Emission type
	 * @return Intensity
	 */
	public Percentile emission(Emission emission) {
		return emissions.getOrDefault(emission, Percentile.ZERO);
	}

	/**
	 * Updates state after a change to the equipment.
	 */
	protected void update() {
		updateEmissions();
		weapon = Optional.ofNullable(findWeapon());
	}

	/**
	 * Updates emissions in this equipment.
	 */
	private void updateEmissions() {
		for(Emission e : Emission.values()) {
			final var max = Thing.max(e, equipment.values().stream());
			emissions.put(e, max);
		}
	}

	/**
	 * @return Equipped weapon
	 */
	private Weapon findWeapon() {
		final WorldObject obj = equipment.get(Slot.MAIN);
		if(obj instanceof Weapon) {
			return (Weapon) obj;
		}
		else {
			return null;
		}
	}
}
