package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Value.Operator;

public class ValueTest {
	private Actor actor;
	
	@Before
	public void before() {
		actor = mock(Actor.class);
	}
	
	@Test
	public void literal() {
		final Value literal = Value.literal(42);
		assertEquals(42, literal.evaluate(actor));
	}
	
	@Test
	public void random() {
		final Value random = Value.random(1);
		assertEquals(0, random.evaluate(actor));
	}
	
	@Test
	public void compound() {
		final Value[] values = new Value[]{Value.literal(1), Value.literal(2)};
		final Value compound = Value.compound(Operator.ADD, Arrays.asList(values));
		assertEquals(3, compound.evaluate(actor));
	}
}
