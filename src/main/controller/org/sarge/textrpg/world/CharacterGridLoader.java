package org.sarge.textrpg.world;

import org.sarge.textrpg.util.Matrix;

/**
 * A <i>character grid loader</i> transforms a 2D text grid to a character {@link Matrix}.
 * @author Sarge
 */
public class CharacterGridLoader {
	/**
	 * Loads a character grid.
	 * @param text Text
	 * @return Character grid
	 * @throws IllegalArgumentException if the text is empty
	 * @throws IllegalArgumentException if any rows are not the same width as the others
	 */
	public Matrix<Character> load(String text) {
		// Split into rows
		final String[] rows = text.trim().split("\\s+");
		if(rows.length == 0) throw new IllegalArgumentException("Empty grid");

		// Determine grid width
		final int width = rows[0].length();
		if(width == 0) throw new IllegalArgumentException("Empty grid");

		// Allocate grid
		final Matrix<Character> grid = new Matrix<>(width, rows.length);

		// Load grid
		for(int row = 0; row < rows.length; ++row) {
			// Check width of this row
			final char[] chars = rows[row].toCharArray();
			if(chars.length != width) throw new IllegalArgumentException("Grid width mismatch at line " + row);

			// Load row
			for(int n = 0; n < width; ++n) {
				grid.set(n, row, chars[n]);
			}
		}

		return grid;
	}
}
