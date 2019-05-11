package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.ArgumentFormatter.Registry;

public class ArgumentFormatterTest {
	@Nested
	class FormatterTests {
		private NameStore store;

		@BeforeEach
		public void before() {
			store = mock(NameStore.class);
		}

		@Test
		public void formatToken() {
			when(store.get("arg")).thenReturn("result");
			assertEquals("result", ArgumentFormatter.TOKEN.format("arg", store));
		}

		@Test
		public void formatPlainString() {
			assertEquals("arg", ArgumentFormatter.PLAIN.format("arg", store));
		}

		@Test
		public void formatPlainObject() {
			assertEquals("42", ArgumentFormatter.PLAIN.format(42, null));
		}

		@Test
		public void formatInteger() {
			final ArgumentFormatter formatter = ArgumentFormatter.integral(String::valueOf);
			assertEquals("42", formatter.format(42, null));
		}
	}

	@Nested
	class RegistryTests {
		private Registry registry;

		@BeforeEach
		public void before() {
			registry = new Registry();
		}

		@Test
		public void get() {
			final ArgumentFormatter formatter = mock(ArgumentFormatter.class);
			registry.add("name", formatter);
			assertEquals(formatter, registry.get("name"));
		}

		@Test
		public void getUnknownFormatter() {
			assertThrows(IllegalArgumentException.class, () -> registry.get("name"));
		}
	}
}
