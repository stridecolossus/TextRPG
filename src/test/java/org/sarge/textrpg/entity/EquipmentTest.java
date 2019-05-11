package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.object.Slot;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Percentile;

public class EquipmentTest {
	private Equipment equipment;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		equipment = new Equipment();
		obj = mock(WorldObject.class);
		when(obj.emission(any(Emission.class))).thenReturn(Percentile.ZERO);
	}

	@Test
	public void constructor() {
		assertNotNull(equipment.equipment());
		assertEquals(true, equipment.equipment().isEmpty());
		assertNotNull(equipment.select(WorldObject.class));
		assertEquals(0, equipment.select(WorldObject.class).count());
		assertEquals(Optional.empty(), equipment.weapon());
		for(Emission e : Emission.values()) {
			assertEquals(Percentile.ZERO, equipment.emission(e));
		}
	}

	@Test
	public void equip() {
		equipment.equip(obj, Slot.BACK);
		assertEquals(true, equipment.contains(obj));
		assertEquals(true, equipment.contains(Slot.BACK));
		assertEquals(obj, equipment.equipment().get(Slot.BACK));
		assertEquals(Slot.BACK, equipment.slot(obj));
		assertEquals(1, equipment.select(WorldObject.class).count());
		assertEquals(obj, equipment.select(WorldObject.class).iterator().next());
	}

	@Test
	public void equipSlotOccupied() {
		equipment.equip(obj, Slot.BACK);
		assertThrows(IllegalStateException.class, () -> equipment.equip(obj, Slot.BACK));
	}

	@Test
	public void remove() {
		equipment.equip(obj, Slot.BACK);
		equipment.remove(Slot.BACK);
		assertEquals(false, equipment.contains(obj));
		assertEquals(false, equipment.contains(Slot.BACK));
		assertEquals(null, equipment.slot(obj));
		assertEquals(true, equipment.equipment().isEmpty());
	}

	@Test
	public void removeSlotNotOccupied() {
		assertThrows(IllegalStateException.class, () -> equipment.remove(Slot.BACK));
	}

	@Test
	public void emissions() {
		when(obj.emission(Emission.LIGHT)).thenReturn(Percentile.HALF);
		equipment.equip(obj, Slot.BACK);
		assertEquals(Percentile.HALF, equipment.emission(Emission.LIGHT));
		assertEquals(Percentile.ZERO, equipment.emission(Emission.SMOKE));
		assertEquals(Percentile.ZERO, equipment.emission(Emission.SOUND));
	}

	@Test
	public void weapon() {
		// Create weapon
		final Weapon weapon = mock(Weapon.class);
		when(weapon.emission(any(Emission.class))).thenReturn(Percentile.ZERO);

		// Wield weapon
		equipment.equip(weapon, Slot.MAIN);
		assertEquals(Optional.of(weapon), equipment.weapon());
		assertEquals(Slot.MAIN, equipment.slot(weapon));
		assertEquals(1, equipment.select(Weapon.class).count());
		assertEquals(weapon, equipment.select(Weapon.class).iterator().next());

		// Remove weapon
		equipment.remove(Slot.MAIN);
		assertEquals(Optional.empty(), equipment.weapon());
	}

	@Test
	public void free() {
		assertEquals(Optional.of(Slot.MAIN), equipment.free());
	}

	@Test
	public void freeMainHand() {
		equipment.equip(obj, Slot.OFF);
		assertEquals(Optional.of(Slot.MAIN), equipment.free());
	}

	@Test
	public void freeOffHand() {
		equipment.equip(obj, Slot.MAIN);
		assertEquals(Optional.of(Slot.OFF), equipment.free());
	}

	@Test
	public void freeNotAvailable() {
		equipment.equip(obj, Slot.MAIN);
		equipment.equip(obj, Slot.OFF);
		assertEquals(Optional.empty(), equipment.free());
	}

	@Test
	public void isTwoHandedAvailable() {
		assertEquals(true, equipment.isTwoHandedAvailable());
	}

	@Test
	public void isTwoHandedAvailableMainHandOccupied() {
		equipment.equip(obj, Slot.MAIN);
		assertEquals(false, equipment.isTwoHandedAvailable());
	}

	@Test
	public void isTwoHandedAvailableOffHandOccupied() {
		equipment.equip(obj, Slot.OFF);
		assertEquals(false, equipment.isTwoHandedAvailable());
	}

	@Test
	public void isTwoHandedAvailableBothHandOccupied() {
		equipment.equip(obj, Slot.MAIN);
		equipment.equip(obj, Slot.OFF);
		assertEquals(false, equipment.isTwoHandedAvailable());
	}

	@Test
	public void clear() {
		equipment.equip(obj, Slot.BACK);
		equipment.clear();
		assertEquals(true, equipment.equipment().isEmpty());
	}

	@Test
	public void clearEmptyEquipment() {
		assertThrows(IllegalStateException.class, () -> equipment.clear());
	}
}
