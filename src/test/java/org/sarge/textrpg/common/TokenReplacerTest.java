package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sarge.lib.util.MapBuilder;
import org.sarge.textrpg.util.TokenReplacer;

public class TokenReplacerTest {
	private TokenReplacer replacer;
	private Map<String, String> tokens;
	
	@Before
	public void before() {
		tokens = MapBuilder.build("1", "one", "2", "two", "3", "{1}", "full.stop", "three");
		replacer = new TokenReplacer(tokens::get);
	}
	
	@Test
	public void replace() {
		final String result = replacer.replace("start {1} middle {2} end");
		assertEquals("start one middle two end", result);
	}
	
	@Test
	public void replaceOnlyTokens() {
		final String result = replacer.replace("{1} {2}");
		assertEquals("one two", result);
	}

	@Test
	public void replaceEmptyText() {
		final String result = replacer.replace("");
		assertEquals("", result);
	}
	
	@Test
	public void replaceInvalidToken() {
		final String result = replacer.replace("start {missing} end");
		assertEquals("start  end", result);
	}
	
	@Test
	public void replaceFullStopToken() {
		final String result = replacer.replace("{full.stop}");
		assertEquals("three", result);
	}

	@Test
	public void replaceRecursive() {
		final String result = replacer.replace("{3}", true);
		assertEquals("one", result);
	}
}
