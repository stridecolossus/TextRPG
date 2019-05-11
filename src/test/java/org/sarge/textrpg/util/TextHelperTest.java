package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.Direction;

public class TextHelperTest {
	@Test
	public void join() {
		final String key = TextHelper.join("one.TWO", 3);
		assertEquals("one.two.3", key);
	}

	@Test
	public void prefix() {
		assertEquals("direction.east", TextHelper.prefix(Direction.EAST));
	}

	@Test
	public void wrap() {
		assertEquals("1str2", TextHelper.wrap("str", '1', '2'));
	}

	@Test
	public void wrapSingleCharacter() {
		assertEquals("-str-", TextHelper.wrap("str", '-'));
	}
}
