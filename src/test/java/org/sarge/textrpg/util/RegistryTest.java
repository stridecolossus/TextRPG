package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class RegistryTest {
	@Test
	public void build() {
		// Build a registry
		final Registry.Builder<Integer> builder = new Registry.Builder<>(String::valueOf);
		builder.add(42);

		// Create registry
		final Registry<Integer> registry = builder.build();
		assertNotNull(registry);

		// Check accessor
		assertEquals(Integer.valueOf(42), registry.get("42"));
		assertThrows(IllegalArgumentException.class, () -> registry.get("cobblers"));
	}
}
