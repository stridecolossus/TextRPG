package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.lang.model.element.Modifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.textrpg.util.MutableIntegerMap.MutableEntry;

public class EnumerationIntegerMapTest {
	private EnumerationIntegerMap<Modifier> map;

	@BeforeEach
	public void before() {
		map = new EnumerationIntegerMap<>(Modifier.class);
	}

	@ParameterizedTest
	@EnumSource(Modifier.class)
	public void entry(Modifier key) {
		final MutableEntry entry = map.get(key);
		assertNotNull(entry);
		entry.modify(1);
		entry.modify(2);
		assertEquals(3, entry.get());
	}

	@Test
	public void copy() {
		final IntegerMap<Modifier> copy = new EnumerationIntegerMap<>(Modifier.class, map);
		map.get(Modifier.NATIVE).set(42);
		assertEquals(0, copy.get(Modifier.NATIVE).get());
	}
}
