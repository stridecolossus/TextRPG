package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.lang.model.element.Modifier;

import org.junit.jupiter.api.Test;

public class DescriptionTest {
	/**
	 * Checks a description argument.
	 * @param description		Description
	 * @param name				Argument name
	 * @param arg				Expected argument value
	 * @param type				Expected formatter
	 */
	private static void check(Description description, String name, Object arg, ArgumentFormatter formatter) {
		final Description.Entry entry = description.get(name);
		assertNotNull(entry);
		assertEquals(arg, entry.argument());
		assertEquals(formatter, entry.formatter());
	}

	@Test
	public void constructorArgument() {
		final Description description = new Description("key", "name", "value");
		assertNotNull(description);
		assertEquals("key", description.key());
		assertNotNull(description.get("name"));
		assertEquals("value", description.get("name").argument());
	}

	@Test
	public void constructorNameArgument() {
		final Description description = new Description("key", "value");
		assertNotNull(description);
		assertEquals("key", description.key());
		assertNotNull(description.get("name"));
		assertEquals("value", description.get("name").argument());
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
			.add("plain", "string", ArgumentFormatter.PLAIN)
			.add("integer", 42)
			.add("enum", Modifier.NATIVE)
			.build();

		// Check description
		assertNotNull(description);
		assertEquals("key", description.key());

		// Check arguments
		check(description, "name", "name", ArgumentFormatter.TOKEN);
		check(description, "plain", "string", ArgumentFormatter.PLAIN);
		check(description, "integer", 42, ArgumentFormatter.PLAIN);
		check(description, "enum", "modifier.native", ArgumentFormatter.TOKEN);
	}

	@Test
	public void equals() {
		final Description description = new Description.Builder("key").add("name", "value").build();
		assertEquals(description, description);
		assertNotEquals(description, null);
		assertNotEquals(description, Description.of("other"));
	}
}
