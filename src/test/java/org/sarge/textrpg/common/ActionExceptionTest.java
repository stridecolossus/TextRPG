package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;

public class ActionExceptionTest {
	@Test
	public void description() {
		final Description description = Description.of("key");
		final ActionException exception = new ActionException(description);
		assertEquals("key", exception.getMessage());
		assertEquals(description, exception.description());
	}

	@Test
	public void of() {
		final ActionException exception = ActionException.of("key");
		assertEquals("key", exception.getMessage());
		assertEquals(Description.of("key"), exception.description());
	}
}
