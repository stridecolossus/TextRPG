package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Equipment;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class SheathActionTest extends ActionTestBase {
	private SheathAction action;
	private Sheath sheath;
	private Weapon weapon;

	@BeforeEach
	public void before() throws ActionException {
		sheath = new Sheath.Descriptor(new ObjectDescriptor.Builder("sheath").slot(Slot.BACK).build(), "cat").create();
		weapon = TestHelper.weapon(skill, Damage.Type.GENERAL);
		action = new SheathAction();
	}

	@Test
	public void sheath() throws ActionException {
		final Equipment equipment = actor.contents().equipment();
		equipment.equip(sheath, Slot.LEGS);
		equipment.equip(weapon, Slot.MAIN);
		assertEquals(Response.OK, action.sheath(actor, sheath));
		assertEquals(Optional.of(weapon), sheath.weapon());
		assertEquals(null, equipment.equipment().get(Slot.MAIN));
	}

	@Test
	public void sheathNotEquipped() throws ActionException {
		TestHelper.expect("sheath.not.equipped", () -> action.sheath(actor, sheath));
	}

	@Test
	public void sheathWeaponNotEquipped() throws ActionException {
		actor.contents().equipment().equip(sheath, Slot.LEGS);
		TestHelper.expect("sheath.requires.weapon", () -> action.sheath(actor, sheath));
	}

	@Test
	public void sheathNotAvailable() throws ActionException {
		TestHelper.expect("sheath.not.found", () -> action.sheath(actor));
	}

	@Test
	public void draw() throws ActionException {
		actor.contents().equipment().equip(sheath, Slot.LEGS);
		sheath.sheath(weapon);
		action.draw(actor);
		assertEquals(Optional.empty(), sheath.weapon());
	}

	@Test
	public void drawRequiresWeapon() throws ActionException {
		actor.contents().equipment().equip(sheath, Slot.LEGS);
		TestHelper.expect("draw.requires.weapon", () -> action.draw(actor));
	}

	@Test
	public void drawWeapon() throws ActionException {
		actor.contents().equipment().equip(sheath, Slot.LEGS);
		sheath.sheath(weapon);
		action.draw(actor, weapon);
		assertEquals(Optional.empty(), sheath.weapon());
	}

	@Test
	public void drawWeaponNotSheathed() throws ActionException {
		TestHelper.expect("draw.not.sheathed", () -> action.draw(actor, weapon));
	}
}
