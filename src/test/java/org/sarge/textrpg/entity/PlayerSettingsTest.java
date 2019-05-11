package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.textrpg.entity.PlayerSettings.Setting;
import org.sarge.textrpg.util.ActionException;

public class PlayerSettingsTest {
	private PlayerSettings settings;

	@BeforeEach
	public void before() {
		settings = new PlayerSettings();
	}

	@ParameterizedTest
	@EnumSource(value=Setting.class, names={"SWIM", "CLIMB_SAFE", "BRIEF", "ALLOW_FOLLOW"})
	public void isBoolean(Setting setting) {
		assertEquals(true, setting.isBoolean());
	}

	@ParameterizedTest
	@EnumSource(value=Setting.class, mode=EnumSource.Mode.EXCLUDE, names={"SWIM", "CLIMB_SAFE", "BRIEF", "ALLOW_FOLLOW"})
	public void isInteger(Setting setting) {
		assertEquals(false, setting.isBoolean());
	}

	@Test
	public void toBooleanInvalid() {
		assertThrows(IllegalStateException.class, () -> settings.toBoolean(Setting.AUTO_FLEE));
	}

	@Test
	public void setBoolean() {
		settings.set(Setting.SWIM, true);
		assertEquals(true, settings.toBoolean(Setting.SWIM));
	}

	@Test
	public void setBooleanInvalid() {
		assertThrows(IllegalStateException.class, () -> settings.set(Setting.AUTO_FLEE, true));
	}

	@Test
	public void toIntegerInvalid() {
		assertThrows(IllegalStateException.class, () -> settings.toInteger(Setting.SWIM));
	}

	@Test
	public void setInteger() {
		settings.set(Setting.AUTO_FLEE, 42);
		assertEquals(42, settings.toInteger(Setting.AUTO_FLEE));
	}

	@Test
	public void setIntegerInvalid() {
		assertThrows(IllegalStateException.class, () -> settings.set(Setting.AUTO_FLEE, true));
	}

	@Test
	public void modify() {
		settings.modify(Setting.AUTO_FLEE, 1);
		settings.modify(Setting.AUTO_FLEE, 2);
		assertEquals(3, settings.toInteger(Setting.AUTO_FLEE));
	}

	@Test
	public void modifyInvalidType() {
		assertThrows(IllegalStateException.class, () -> settings.modify(Setting.SWIM, 42));
	}

	@Test
	public void modifyInvalidValue() {
		assertThrows(IllegalStateException.class, () -> settings.modify(Setting.AUTO_FLEE, -1));
	}

	@Test
	public void transaction() throws ActionException {
		final Transaction tx = settings.transaction(Setting.CASH, 2, "message");
		settings.set(Setting.CASH, 3);
		tx.check();
		tx.complete();
		assertEquals(1, settings.toInteger(Setting.CASH));
	}

	@Test
	public void transactionInvalid() throws ActionException {
		assertThrows(IllegalArgumentException.class, () -> settings.transaction(Setting.AUTO_FLEE, 1, "message"));
	}
}
