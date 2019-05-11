package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class WhetActionTest extends ActionTestBase {
	private WhetAction action;
	private Weapon weapon;

	@BeforeEach
	public void before() throws ActionException {
		action = new WhetAction(DURATION);
		weapon = TestHelper.weapon(skill, Damage.Type.PIERCING);
		actor.contents().equipment().equip(weapon, Slot.MAIN);
	}

	private void addStamina() {
		actor.model().values().get(EntityValue.STAMINA.key()).set(1);
	}

	@Test
	public void whet() throws ActionException {
		// Apply wear
		addStamina();
		weapon.use();

		// Start whet
		final Response response = action.whet(actor);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(true, descriptor.isFlag(Induction.Flag.REPEATING));

		// Iterate and check whet finished
		TestHelper.expect("whet.finished", () -> complete(response));
		assertEquals(0, actor.model().values().get(EntityValue.STAMINA.key()).get());
	}

	@Test
	public void whetRequiresWeapon() throws ActionException {
		actor.contents().equipment().remove(Slot.MAIN);
		weapon.use();
		addStamina();
		TestHelper.expect("whet.requires.weapon", () -> action.whet(actor));
	}

	@Test
	public void whetWeapon() throws ActionException {
		addStamina();
		weapon.use();
		action.whet(actor, weapon);
	}

	@Test
	public void whetInvalidWeapon() throws ActionException {
		weapon = TestHelper.weapon(skill, Damage.Type.FIRE);
		TestHelper.expect("whet.invalid.weapon", () -> action.whet(actor, weapon));
	}

	@Test
	public void whetNotDamaged() throws ActionException {
		TestHelper.expect("whet.not.damaged", () -> action.whet(actor, weapon));
	}

	@Test
	public void whetExhausted() throws ActionException {
		weapon.use();
		TestHelper.expect("whet.exhausted", () -> action.whet(actor, weapon));
	}
}
