package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.DeploymentSlot;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.object.WorldObject;

public class EquipmentTest extends ActionTest {
	private static final DeploymentSlot SLOT = DeploymentSlot.MAIN_HAND;

	private Equipment equipment;
	private WorldObject obj;

	@Before
	public void before() {
		final ObjectDescriptor.Equipment equip = new ObjectDescriptor.EquipmentBuilder().slot(SLOT).armour(3).build();
		obj = new Builder("object").weight(2).equipment(equip).build().create();
		equipment = new Equipment();
	}

	@Test
	public void constructor() {
		assertNotNull(equipment.stream());
		assertEquals(0, equipment.stream().count());
		assertEquals(0, equipment.getWeight());
	}

	@Test
	public void equip() throws ActionException {
		equipment.equip(obj);
		assertEquals(Optional.of(obj), equipment.get(SLOT));
		assertEquals(2, equipment.getWeight());
	}

	@Test
	public void equipCannotEquip() throws ActionException {
		final WorldObject invalid = new Builder("invalid").build().create();
		expect("equipment.cannot.equip");
		equipment.equip(invalid);
	}

	private static WorldObject createTwoHanded() {
		final ObjectDescriptor.Equipment equip = new ObjectDescriptor.EquipmentBuilder().slot(DeploymentSlot.OFF_HAND).twoHanded(true).build();
		return new Builder("off.hand").equipment(equip).build().create();
	}

	@Test
	public void equipTwoHanded() throws ActionException {
		final WorldObject other = createTwoHanded();
		equipment.equip(obj);
		expect("equipment.two.handed");
		equipment.equip(other);
	}

	@Test
	public void equipTwoHandedEquipped() throws ActionException {
		final WorldObject other = createTwoHanded();
		equipment.equip(other);
		expect("equipment.two.handed");
		equipment.equip(obj);
	}

	@Test
	public void equipOccupied() throws ActionException {
		equipment.equip(obj);
		expect("equipment.equip.occupied");
		equipment.equip(obj);
	}

	@Test
	public void remove() throws ActionException {
		equipment.equip(obj);
		equipment.remove(obj);
		assertEquals(0, equipment.stream().count());
		assertEquals(0, equipment.getWeight());
	}

	@Test
	public void removeEmpty() throws ActionException {
		expect("equipment.remove.empty");
		equipment.remove(obj);
	}

	@Test
	public void removeAll() throws ActionException {
		equipment.equip(obj);
		equipment.removeAll();
		assertEquals(0, equipment.stream().count());
		assertEquals(0, equipment.getWeight());
	}

	@Test
	public void removeAllEmpty() throws ActionException {
		expect("equipment.remove.all");
		equipment.removeAll();
	}

	@Test
	public void describe() throws ActionException {
		equipment.equip(obj);
		final List<Description> desc = equipment.describe();
		assertNotNull(desc);
		assertEquals(1, desc.size());
	}
}
