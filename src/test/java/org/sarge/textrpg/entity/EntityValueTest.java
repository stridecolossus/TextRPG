package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.textrpg.entity.EntityValue.Key;
import org.sarge.textrpg.entity.EntityValue.Type;

public class EntityValueTest {
	@Nested
	class EntityValueTests {
		@ParameterizedTest
		@EnumSource(EntityValue.class)
		public void mnemonic(EntityValue value) {
			assertNotNull(value.mnemonic());
		}

		@ParameterizedTest
		@EnumSource(value=EntityValue.class, names={"HEALTH", "STAMINA", "POWER"})
		public void isPrimary(EntityValue value) {
			assertEquals(true, value.isPrimary());
		}

		@ParameterizedTest
		@EnumSource(value=EntityValue.class, mode=EnumSource.Mode.EXCLUDE, names={"HEALTH", "STAMINA", "POWER"})
		public void notPrimary(EntityValue value) {
			assertEquals(false, value.isPrimary());
		}

		@ParameterizedTest
		@EnumSource(value=EntityValue.class, names={"THIRST", "HUNGER", "MOVEMENT_COST"})
		public void isPercentile(EntityValue value) {
			assertEquals(Type.PERCENTILE, value.type());
		}

		@ParameterizedTest
		@EnumSource(value=EntityValue.class, names={"PANIC", "WARMTH"})
		public void isPositive(EntityValue value) {
			assertEquals(Type.POSITIVE, value.type());
		}

		@ParameterizedTest
		@EnumSource(value=EntityValue.class, names={"BLOCK", "PARRY", "DODGE", "ARMOUR"})
		public void isDefault(EntityValue value) {
			assertEquals(Type.DEFAULT, value.type());
		}

		@Test
		public void visibility() {
			assertEquals(Type.VISIBILITY, EntityValue.VISIBILITY.type());
		}
	}

	@Nested
	class KeyTests {
		@ParameterizedTest
		@EnumSource(value=EntityValue.class, names={"HEALTH", "STAMINA", "POWER"})
		public void constructorMaximum(EntityValue value) {
			final Key key = value.key(Key.Type.MAXIMUM);
			assertEquals(value, key.value());
			assertEquals(Key.Type.MAXIMUM, key.type());
		}

		@ParameterizedTest
		@EnumSource(value=EntityValue.class, names={"HEALTH", "STAMINA", "POWER"})
		public void constructorRegeneration(EntityValue value) {
			final Key key = value.key(Key.Type.REGENERATION);
			assertEquals(value, key.value());
			assertEquals(Key.Type.REGENERATION, key.type());
		}

		@ParameterizedTest
		@EnumSource(EntityValue.class)
		public void key(EntityValue value) {
			final Key key = value.key();
			assertNotNull(key);
			assertEquals(value, key.value());
			assertEquals(Key.Type.DEFAULT, key.type());
		}

		@Test
		public void constructorInvalid() {
			assertThrows(IllegalArgumentException.class, () -> EntityValue.ARMOUR.key(Key.Type.MAXIMUM));
			assertThrows(IllegalArgumentException.class, () -> EntityValue.ARMOUR.key(Key.Type.REGENERATION));
		}
	}
}
