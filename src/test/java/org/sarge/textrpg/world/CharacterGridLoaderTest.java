package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Matrix;

public class CharacterGridLoaderTest {
	private CharacterGridLoader loader;

	@BeforeEach
	public void before() {
		loader = new CharacterGridLoader();
	}

	@Test
	public void load() {
		final Matrix<Character> matrix = loader.load("ab\ncd");
		assertNotNull(matrix);
		assertEquals(2, matrix.width());
		assertEquals(2, matrix.height());
		assertEquals(Character.valueOf('a'), matrix.get(0, 0));
		assertEquals(Character.valueOf('b'), matrix.get(1, 0));
		assertEquals(Character.valueOf('c'), matrix.get(0, 1));
		assertEquals(Character.valueOf('d'), matrix.get(1, 1));
	}

	@Test
	public void loadEmptyGrid() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(""));
	}

	@Test
	public void loadEmptyRow() {
		assertThrows(IllegalArgumentException.class, () -> loader.load("\n"));
	}

	@Test
	public void loadMismatchedRow() {
		assertThrows(IllegalArgumentException.class, () -> loader.load("ab\nc"));
	}
}
