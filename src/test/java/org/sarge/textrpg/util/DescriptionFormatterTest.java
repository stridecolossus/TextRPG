package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DescriptionFormatterTest {
	private DescriptionFormatter formatter;
	private NameStore store;

	@BeforeEach
	public void before() {
		// Create store with description and argument templates
		final var map = Map.of(
			"key", "start {name} end",
			"arg", "before {sub} {ignored} after",
			"pad", "before {padding:10} after"
		);
		store = new DefaultNameStore(map);

		// Create formatter
		formatter = new DescriptionFormatter();
	}

	@Test
	public void formatTokenReplacement() {
		final var description = new Description.Builder("key").name("arg").add("sub", 42).build();
		final String expected = "start before 42 {ignored} after end";
		assertEquals(expected, formatter.format(description, store));
	}

	@Test
	public void formatUnknownKey() {
		final var description = Description.of("unknown");
		final var result = formatter.format(description, store);
		assertEquals(true, result.isEmpty());
	}

	@Test
	public void formatNullValue() {
		final ArgumentFormatter invalid = (arg, store) -> null;
		final var description = new Description.Builder("key").add("name", "text", invalid).build();
		assertEquals("start {name} end", formatter.format(description, store));
	}

	@Test
	public void formatPadding() {
		// TODO
		//assertEquals("before     after", formatter.format(new Description("pad"), store));
	}

	@Test
	public void formatEmptyArgument() {
		final Description description = new Description.Builder("key").name(StringUtils.EMPTY).build();
		assertEquals("start end", formatter.format(description, store));
	}
}
