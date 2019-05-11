package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.textrpg.entity.EntityValue.Key;
import org.sarge.textrpg.util.IntegerMap.Entry;
import org.sarge.textrpg.util.MutableIntegerMap.MutableEntry;
import org.sarge.textrpg.util.Percentile;

public class EntityValueIntegerMapTest {
	private EntityValueIntegerMap map;

	@BeforeEach
	public void before() {
		map = new EntityValueIntegerMap();
	}

	@ParameterizedTest
	@EnumSource(value=EntityValue.class, mode=EnumSource.Mode.EXCLUDE, names={"VISIBILITY"})
	public void get(EntityValue value) {
		final Entry entry = map.get(value.key());
		assertNotNull(entry);
		assertEquals(0, entry.get());
	}

	@ParameterizedTest
	@EnumSource(value=EntityValue.class, names={"HEALTH", "STAMINA", "POWER"})
	public void primary(EntityValue value) {
		assertNotNull(map.get(value.key(Key.Type.MAXIMUM)));
		assertNotNull(map.get(value.key(Key.Type.REGENERATION)));
	}

	@Test
	public void setClamped() {
		final MutableEntry entry = map.get(EntityValue.ARMOUR.key());
		entry.set(-1);
		assertEquals(0, entry.get());
	}

	@Test
	public void incrementClamped() {
		final MutableEntry entry = map.get(EntityValue.ARMOUR.key());
		entry.modify(-1);
		assertEquals(0, entry.get());
	}

	@Test
	public void setPositiveInvalid() {
		assertThrows(IllegalArgumentException.class, () -> map.get(EntityValue.THIRST.key()).set(-1));
	}

	@Test
	public void incrementPositiveInvalid() {
		assertThrows(IllegalArgumentException.class, () -> map.get(EntityValue.THIRST.key()).modify(-1));
	}

	@Test
	public void setPercentile() {
		final MutableEntry entry = map.get(EntityValue.THIRST.key());
		entry.set(999);
		assertEquals(100, entry.get());
	}

	@Test
	public void incrementPercentile() {
		final MutableEntry entry = map.get(EntityValue.THIRST.key());
		entry.modify(999);
		assertEquals(100, entry.get());
	}

	@Test
	public void incrementPrimary() {
		final MutableEntry primary = map.get(EntityValue.STAMINA.key());
		map.get(EntityValue.STAMINA.key(Key.Type.MAXIMUM)).set(3);
		primary.modify(1.5f);
		assertEquals(1, primary.get());
		primary.modify(2f);
		assertEquals(3, primary.get());
	}

	@Test
	public void incrementPrimaryClamped() {
		final MutableEntry max = map.get(EntityValue.STAMINA.key(Key.Type.MAXIMUM));
		final MutableEntry primary = map.get(EntityValue.STAMINA.key());
		max.set(1);
		primary.modify(2);
		assertEquals(1, primary.get());
	}

	@Test
	public void setPrimaryAndMaximum() {
		final MutableEntry primary = map.get(EntityValue.STAMINA.key());
		primary.set(1);
		assertEquals(1, primary.get());
		assertEquals(1, map.get(EntityValue.STAMINA.key(Key.Type.MAXIMUM)).get());
	}

	@Test
	public void incrementMaximum() {
		final MutableEntry max = map.get(EntityValue.STAMINA.key(Key.Type.MAXIMUM));
		max.set(1);
		assertEquals(1, max.get());
		assertEquals(0, map.get(EntityValue.STAMINA.key()).get());
	}

	@Test
	public void visibility() {
		assertNotNull(map.visibility());
		assertEquals(Percentile.MAX, map.get(EntityValue.VISIBILITY.key()).get());
		assertEquals(Percentile.ONE, map.visibility().get());
	}

	@Test
	public void modifyVisibility() {
		map.get(EntityValue.VISIBILITY.key()).modify(50);
		assertEquals(50, map.get(EntityValue.VISIBILITY.key()).get());
		assertEquals(Percentile.HALF, map.visibility().get());
	}

	@Test
	public void transaction() {
		final MutableEntry entry = map.get(EntityValue.POWER.key());
		final Transaction transaction = map.transaction(EntityValue.POWER, 2, "doh");
		assertNotNull(transaction);
		entry.set(3);
		transaction.complete();
		assertEquals(1, entry.get());
	}

	@Test
	public void transactionInvalid() {
		assertThrows(IllegalArgumentException.class, () -> map.transaction(EntityValue.ARMOUR, 42, "doh"));
	}
}
