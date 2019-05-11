package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.IntegerMap.Entry;
import org.sarge.textrpg.util.MutableIntegerMap.MutableEntry;

public class MutableIntegerMapTest {
	private MutableIntegerMap<String> map;

	@BeforeEach
	public void before() {
		map = new MutableIntegerMap<>();
	}

	@Test
	public void get() {
		final Entry entry = map.get("key");
		assertNotNull(entry);
		assertEquals(0, entry.get());
	}

	@Test
	public void set() {
		final MutableEntry entry = map.get("key");
		entry.set(42);
		assertEquals(42, entry.get());
	}

	@Test
	public void modify() {
		final MutableEntry entry = map.get("key");
		entry.modify(1);
		entry.modify(2);
		assertEquals(3, entry.get());
	}
}
