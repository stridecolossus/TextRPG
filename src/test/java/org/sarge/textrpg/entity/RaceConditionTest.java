package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.util.Description;

public class RaceConditionTest {
	private Condition condition;

	@BeforeEach
	public void before() {
		condition = new RaceCondition("cat");
	}

	@Test
	public void reason() {
		assertEquals(new Description("condition.race", "cat"), condition.reason());
	}

	@Test
	public void matches() {
		final Actor actor = mock(Actor.class);
		assertEquals(false, condition.matches(actor));
		when(actor.isRaceCategory("cat")).thenReturn(true);
		assertEquals(true, condition.matches(actor));
	}
}
