package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.TestHelper;

public class ObjectStackTest {
	private ObjectStack stack;

	@BeforeEach
	public void before() {
		stack = new ObjectStack(new ObjectDescriptor.Builder("stack").weight(2).build(), 3);
	}

	@Test
	public void constructor() {
		assertEquals("stack", stack.name());
		assertEquals(3, stack.count());
		assertEquals(2 * 3, stack.weight());
		assertEquals(false, stack.isEmpty());
	}

	@Test
	public void value() {
		assertThrows(UnsupportedOperationException.class, stack::value);
	}

	@Test
	public void modify() {
		stack.modify(2);
		assertEquals(5, stack.count());
		assertEquals(false, stack.isEmpty());
	}

	@Test
	public void modifyEmpty() {
		stack.modify(-3);
		assertEquals(0, stack.count());
		assertEquals(true, stack.isEmpty());
	}

	@Test
	public void invalidModify() {
		assertThrows(IllegalArgumentException.class, () -> stack.modify(-4));
	}

	@Test
	public void destroy() {
		stack.parent(TestHelper.parent());
		stack.destroy();
		assertEquals(0, stack.count());
		assertEquals(true, stack.isEmpty());
		assertEquals(false, stack.isAlive());
	}
}
