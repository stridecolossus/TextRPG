package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ObjectStackTest {
	private ObjectStack stack;
	private ObjectDescriptor descriptor;
	
	@Before
	public void before() {
		descriptor = new ObjectDescriptor("object");
		stack = new ObjectStack(descriptor, 2);
	}
	
	@Test
	public void constructor() {
		assertEquals(descriptor, stack.getDescriptor());
		assertEquals(2, stack.size());
	}
	
	@Test
	public void pop() {
		final WorldObject obj = stack.pop();
		assertEquals(descriptor, obj.getDescriptor());
		assertEquals(1, stack.size());
	}
	
	@Test
	public void popSelf() {
		stack.pop();
		final WorldObject obj = stack.pop();
		assertEquals(stack, obj);
		assertEquals(1, stack.size());
	}
	
	@Test
	public void add() {
		stack.add(1);
		assertEquals(3, stack.size());
	}
	
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void invalidResetPeriod() {
		new ObjectStack(new ObjectDescriptor.Builder("reset").reset(42).build(), 2);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void invalidForgetPeriod() {
		new ObjectStack(new ObjectDescriptor.Builder("forget").forget(42).build(), 2);
	}
}
