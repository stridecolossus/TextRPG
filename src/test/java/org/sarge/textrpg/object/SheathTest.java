package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class SheathTest {
	private Sheath sheath;
	private Weapon weapon;

	@BeforeEach
	public void before() {
		final var descriptor = new ObjectDescriptor.Builder("sheath").size(Size.MEDIUM).build();
		sheath = new Sheath(new Sheath.Descriptor(descriptor, "weapon"));
		weapon = mock(Weapon.class);
		when(weapon.size()).thenReturn(Size.MEDIUM);
		when(weapon.isCategory("weapon")).thenReturn(true);
	}

	@Test
	public void constructor() {
		assertEquals("sheath", sheath.name());
		assertNotNull(sheath.descriptor());
		assertEquals(Optional.empty(), sheath.weapon());
	}

	@Test
	public void sheath() throws ActionException {
		when(weapon.isCategory("weapon")).thenReturn(true);
		sheath.sheath(weapon);
		assertEquals(Optional.of(weapon), sheath.weapon());
	}

	@Test
	public void sheathOccupied() throws ActionException {
		sheath.sheath(weapon);
		TestHelper.expect("sheath.occupied", () -> sheath.sheath(weapon));
	}

	@Test
	public void sheathInvalidWeapon() throws ActionException {
		when(weapon.isCategory("weapon")).thenReturn(false);
		TestHelper.expect("sheath.invalid.weapon", () -> sheath.sheath(weapon));
	}

	@Test
	public void sheathTooSmall() throws ActionException {
		when(weapon.size()).thenReturn(Size.LARGE);
		TestHelper.expect("sheath.too.small", () -> sheath.sheath(weapon));
	}

	@Test
	public void clear() throws ActionException {
		sheath.sheath(weapon);
		sheath.clear();
		assertEquals(Optional.empty(), sheath.weapon());
	}

	@Test
	public void clearNotOccupied() throws ActionException {
		TestHelper.expect("sheath.not.occupied", sheath::clear);
	}
}
