package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import org.junit.Test;

public class DescriptionTest {
	@Test
	public void constructor() {
		final Description description = new Description("key");
		assertEquals("key", description.getKey());
		assertEquals(false, description.isFullStop());
		assertNotNull(description.getDescriptions());
		assertEquals(0, description.getDescriptions().count());
	}

	@Test
	public void builder() {
		final Description description = new Description.Builder("key")
			.add("arg", "value")
			.add("empty", Optional.empty())
			.wrap("wrap", "value")
			.add(new Description("sub"))
			.stop()
			.build();
		assertEquals("key", description.getKey());
		assertEquals(true, description.isFullStop());
		assertEquals("value", description.get("arg"));
		assertEquals(null, description.get("empty"));
		assertEquals("{value}", description.get("wrap"));
		assertEquals(1, description.getDescriptions().count());
		assertEquals("sub", description.getDescriptions().iterator().next().getKey());
	}
	
	@Test
	public void toNotification() {
		final Description description = new Description("key");
		final Notification n = description.toNotification();
		assertNotNull(n);
		assertEquals(description, n.describe());
	}
}
