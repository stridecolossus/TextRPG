package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.util.DescriptionFormatter;

public class DescriptionFormatterTest {
	private DescriptionFormatter formatter;
	
	@Before
	public void before() {
		formatter = new DescriptionFormatter(Function.identity());
	}
	
	@Test
	public void format() {
		final Description description = new Description.Builder("a {size} piece of cheese").add("size", "small").build();
		final String result = formatter.format(description);
		assertEquals("a small piece of cheese", result);
	}
	
	@Test
	public void formatRecurse() {
		final Description child = new Description("child");
		final Description desc = Description.create("parent", Arrays.asList(child));
		final String result = formatter.format(desc);
		assertEquals("parent\nchild", result);
	}
}
