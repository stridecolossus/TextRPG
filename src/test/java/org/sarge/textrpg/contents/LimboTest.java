package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class LimboTest {
	@Test
	public void constructor() {
		assertEquals(null, Parent.LIMBO.parent());
		assertNotNull(Parent.LIMBO.contents());
	}
}
