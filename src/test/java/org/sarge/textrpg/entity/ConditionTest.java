package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Condition;

public class ConditionTest {
	private Actor actor;
	
	@Before
	public void before() {
		actor = mock(Actor.class);
	}
	
	@Test
	public void compound() {
		assertEquals(true, Condition.compound(Collections.singletonList(Condition.TRUE)).evaluate(actor));
	}
	
	@Test
	public void invert() {
		assertEquals(false, Condition.invert(Condition.TRUE).evaluate(actor));
	}
}
