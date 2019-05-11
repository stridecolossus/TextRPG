package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EmptyNameStoreTest {
	@Test
	public void empty() {
		assertEquals(true, NameStore.EMPTY.isEmpty());
		assertEquals(null, NameStore.EMPTY.get("key"));
		assertEquals(false, NameStore.EMPTY.matches("key", "name"));
	}
}
