package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.NameStore.ArrayNameStore;
import org.sarge.textrpg.util.NameStore.DefaultNameStore;

public class NameStoreTest {
	@Nested
	class EmptyNameStoreTests {
		@Test
		public void isEmpty() {
			assertEquals(true, NameStore.EMPTY.isEmpty());
		}
	}

	@Nested
	class DefaultNameStoreTests {
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

	@Nested
	class ArrayNameStoreTests {
		private NameStore store;

		@BeforeEach
		public void before() {
			store = new ArrayNameStore(Map.of("key", new String[]{"one", "two"}));
		}

		@Test
		public void constructor() {
			assertEquals(false, store.isEmpty());
		}

		@Test
		public void get() {
			assertEquals("one", store.get("key"));
		}

		@Test
		public void getUnknown() {
			assertEquals(null, store.get("cobblers"));
		}

		@Test
		public void matches() {
			assertTrue(store.matches("key", "one"));
			assertTrue(store.matches("key", "two"));
		}

		@Test
		public void matchesNotMatched() {
			assertFalse(store.matches("key", "cobblers"));
		}
	}

	@Nested
	class CompoundNameStoreTests {
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
}
