package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TextManglerTest {
	@Test
	public void mangle() {
		final String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
		final TextMangler mangler = new TextMangler(Percentile.HALF);
		final String mangled = mangler.mangle(text);
		assertNotEquals(text, mangled);
		assertTrue(text.length() == mangled.length());
	}

	@Test
	public void constructorInvalidScore() {
		assertThrows(IllegalArgumentException.class, () -> new TextMangler(Percentile.ZERO));
		assertThrows(IllegalArgumentException.class, () -> new TextMangler(Percentile.ONE));
	}
}
