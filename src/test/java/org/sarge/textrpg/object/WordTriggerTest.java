package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WordTriggerTest {
	private WordTrigger trigger;

	@BeforeEach
	public void before() {
		trigger = new WordTrigger("name", mock(Control.Handler.class));
	}

	@Test
	public void constructor() {
		assertEquals("name", trigger.name());
		assertNotNull(trigger.handler());
	}
}
