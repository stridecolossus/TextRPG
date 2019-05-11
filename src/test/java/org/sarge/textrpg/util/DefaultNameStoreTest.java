package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultNameStoreTest {
	private NameStore store;

	@BeforeEach
	public void before() {
		store = new DefaultNameStore(Map.of("key", "name"));
	}

	@Test
	public void constructor() {
		assertEquals(false, store.isEmpty());
	}

	@Test
	public void get() {
		assertEquals("name", store.get("key"));
	}

	@Test
	public void getUnknown() {
		assertEquals(null, store.get("cobblers"));
	}

	@Test
	public void matches() {
		assertTrue(store.matches("key", "name"));
	}

	@Test
	public void matchesNotMatched() {
		assertFalse(store.matches("key", "cobblers"));
	}
}
