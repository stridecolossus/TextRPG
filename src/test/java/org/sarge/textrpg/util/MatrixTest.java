package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Matrix.Coordinates;

public class MatrixTest {
	private Matrix<Object> matrix;

	@BeforeEach
	public void before() {
		matrix = new Matrix<>(2, 3);
	}

	@Nested
	class CoordinatesTests {
		private Coordinates coords;

		@BeforeEach
		public void before() {
			coords = new Coordinates(1, 2);
		}

		@Test
		public void constructor() {
			assertNotNull(coords);
			assertEquals(1, coords.x);
			assertEquals(2, coords.y);
			assertEquals(false, matrix.isOutOfBounds(coords));
		}

		@Test
		public void outOfBounds() {
			assertEquals(true, matrix.isOutOfBounds(new Coordinates(2, 3)));
		}

		@Test
		public void equals() {
			assertTrue(coords.equals(coords));
			assertTrue(coords.equals(new Coordinates(1, 2)));
			assertFalse(coords.equals(new Coordinates(0, 0)));
			assertFalse(coords.equals(null));
		}

		@Test
		public void coordinatesHashCode() {
			assertEquals(coords.hashCode(), new Coordinates(1, 2).hashCode());
		}

		@Test
		public void parse() {
			assertEquals(coords, Coordinates.parse("1/2"));
		}

		@Test
		public void parseEmpty() {
			assertThrows(NumberFormatException.class, () -> Coordinates.parse(" "));
		}

		@Test
		public void parseInvalid() {
			assertThrows(NumberFormatException.class, () -> Coordinates.parse("1-2"));
		}
	}

	@Nested
	class MatrixTests {
		@Test
		public void constructor() {
			assertEquals(2, matrix.width());
			assertEquals(3, matrix.height());
		}

		@Test
		public void get() {
			assertEquals(null, matrix.get(1, 2));
			assertEquals(null, matrix.get(new Coordinates(1, 2)));
		}

		@Test
		public void set() {
			final Coordinates coords = new Coordinates(1, 2);
			final Object obj = new Object();
			matrix.set(1, 2, obj);
			assertEquals(obj, matrix.get(coords));
		}

		@Test
		public void fill() {
			final Object obj = new Object();
			matrix.fill(obj);
			for(int x = 0; x < 2; ++x) {
				for(int y = 0; y < 3; ++y) {
					assertEquals(obj, matrix.get(x, y));
				}
			}
		}

		@Test
		public void map() {
			// Populate matrix
			final Object obj = new Object();
			matrix.set(0, 0, obj);

			// Map matrix
			final Function<Object, String> mapper = in -> "string";
			final Matrix<String> result = matrix.map(mapper, false);
			assertNotNull(result);
			assertEquals(matrix.width(), result.width());
			assertEquals(matrix.height(), result.height());

			// Check mapped matrix
			for(int x = 0; x < 2; ++x) {
				for(int y = 0; y < 3; ++y) {
					assertEquals("string", result.get(x, y));
				}
			}
		}
	}
}
