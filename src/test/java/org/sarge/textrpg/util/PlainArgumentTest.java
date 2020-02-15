package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.ArgumentFormatter.PlainArgument;
import org.sarge.textrpg.util.ArgumentFormatter.PlainArgument.PlainArgumentFormatter;

public class PlainArgumentTest {
	private PlainArgumentFormatter formatter;

	@BeforeEach
	public void before() {
		formatter = new PlainArgumentFormatter();
	}

	@Test
	public void formatString() {
		final PlainArgument arg = new PlainArgument("arg");
		assertEquals("arg", formatter.format(arg, null));
	}

	@Test
	public void formatInteger() {
		final PlainArgument arg = new PlainArgument(42);
		assertEquals("42", formatter.format(arg, null));
	}
}
