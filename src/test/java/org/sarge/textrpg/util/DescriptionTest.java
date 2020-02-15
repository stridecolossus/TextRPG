package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.lang.model.element.Modifier;

import org.junit.jupiter.api.Test;

public class DescriptionTest {
	@Test
	public void constructor() {
		final Description description = new Description("key", "name", "value");
		assertNotNull(description);
		assertEquals("key", description.key());
		assertNotNull(description.get("name"));
		assertEquals("value", description.get("name"));
	}

	@Test
	public void constructorNameArgument() {
		final Description description = new Description("key", "value");
		assertNotNull(description);
		assertEquals("key", description.key());
		assertNotNull(description.get("name"));
		assertEquals("value", description.get("name"));
	}

	@Test
	public void of() {
		final Description description = Description.of("key");
		assertNotNull(description);
		assertEquals("key", description.key());
	}

	@Test
	public void builder() {
		// Create a description
		final Description description = new Description.Builder("key")
			.name("name")
			.add("enum", Modifier.NATIVE)
			.build();

		// Check description
		assertNotNull(description);
		assertEquals("key", description.key());

		// Check arguments
		assertEquals("name", description.get("name"));
		assertEquals("modifier.native", description.get("name"));
	}

	@Test
	public void equals() {
		final Description description = new Description.Builder("key").add("name", "value").build();
		assertEquals(description, description);
		assertNotEquals(description, null);
		assertNotEquals(description, Description.of("other"));
	}
}
