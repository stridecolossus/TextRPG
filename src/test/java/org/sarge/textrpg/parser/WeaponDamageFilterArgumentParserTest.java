package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;

public class WeaponDamageFilterArgumentParserTest {
	private WeaponDamageFilterArgumentParser parser;

	@BeforeEach
	public void before() {
		parser = new WeaponDamageFilterArgumentParser();
	}

	@Test
	public void parse() {
		// Create name-store
		final NameStore store = mock(NameStore.class);
		when(store.matches("filter.weapons", "weapons")).thenReturn(true);
		when(store.matches("filter.that", "that")).thenReturn(true);
		when(store.matches("damage.piercing", "pierce")).thenReturn(true);

		// Parse filter
		final WordCursor cursor = new WordCursor("weapons that pierce", store, Set.of());
		final ObjectDescriptor.Filter filter = parser.parse(cursor);
		assertNotNull(filter);

		// Check filter
		final Weapon.Descriptor weapon = mock(Weapon.Descriptor.class);
		when(weapon.damage()).thenReturn(new Damage(Damage.Type.PIERCING, Calculation.ZERO, Effect.NONE));
		assertEquals(true, filter.test(weapon));
		assertEquals(false, filter.test(ObjectDescriptor.of("invalid")));
	}
}
