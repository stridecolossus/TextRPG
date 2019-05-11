package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NameStoreTest {
	private NameStore store;

	@BeforeEach
	public void before() {
		store = mock(NameStore.class);
		when(store.isEmpty()).thenReturn(false);
	}

	@Test
	public void sameStore() {
		assertEquals(store, NameStore.of(store, store));
	}

	@Test
	public void leftStore() {
		assertEquals(store, NameStore.of(NameStore.EMPTY, store));
	}

	@Test
	public void rightStore() {
		assertEquals(store, NameStore.of(store, NameStore.EMPTY));
	}

	@Test
	public void compound() {
		// Create another store
		final NameStore other = mock(NameStore.class);
		when(other.isEmpty()).thenReturn(false);

		// Create compound store
		final NameStore result = NameStore.of(store, other);
		assertNotNull(result);

		// Check lookup delegates
		final String key = "key";
		result.get(key);
		verify(store).get(key);
		verify(other).get(key);

		// Check matches delegates
		result.matches(key, key);
		verify(store).matches(key, key);
		verify(other).matches(key, key);
	}
}
