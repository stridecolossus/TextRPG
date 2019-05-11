package org.sarge.textrpg.util;

import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Mutable 2D matrix.
 * @author Sarge
 * @param <T> Component type
 */
public class Matrix<T> {
	/**
	 * Matrix coordinates.
	 */
	public static class Coordinates {
		/**
		 * Parses coordinates from the given string.
		 * Coordinates are specified as integer values separated by a command or a forward slash character, e.g. <tt>2,3</tt> or <tt>2/3</tt>
		 * @param coords Coordinates string
		 * @return Parsed coordinates
		 * @throws NumberFormatException if the given string is not valid
		 */
		public static Coordinates parse(String coords) {
			// Tokenize
			final String[] parts = coords.trim().split("/|,");
			if(parts.length != 2) throw new NumberFormatException(String.format("Invalid coordinates: [%s]", coords));

			// Parse to coordinates
			final int x = Integer.parseInt(parts[0].trim());
			final int y = Integer.parseInt(parts[1].trim());
			return new Coordinates(x, y);
		}

		public final int x;
		public final int y;

		private final int hash;

		/**
		 * Constructor.
		 * @param x
		 * @param y
		 */
		public Coordinates(int x, int y) {
			this.x = x;
			this.y = y;
			this.hash = x | (y << 15);			// https://stackoverflow.com/questions/22826326/good-hashcode-function-for-2d-coordinates
		}

		/**
		 * Copy constructor.
		 * @param coords Coordinates
		 */
		protected Coordinates(Coordinates coords) {
			this(coords.x, coords.y);
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this) return true;
			if(obj == null) return false;
			if(obj instanceof Coordinates) {
				final Coordinates that = (Coordinates) obj;
				return (this.x == that.x) && (this.y == that.y);
			}
			else {
				return false;
			}
		}

		@Override
		public String toString() {
			return x + "," + y;
		}
	}

	private final Object[][] matrix;

	/**
	 * Constructor.
	 * @param w Width
	 * @param h Height
	 */
	public Matrix(int w, int h) {
		matrix = new Object[w][h];
	}

	/**
	 * @return Width of this matrix
	 */
	public int width() {
		return matrix.length;
	}

	/**
	 * @return Height of this matrix
	 */
	public int height() {
		return matrix[0].length;
	}

	/**
	 * Tests whether the given coordinates are out-of-bounds for this matrix.
	 * @param coords Coordinates
	 * @return Whether out-of-bounds
	 */
	public boolean isOutOfBounds(Coordinates coords) {
		return isOutOfBoundsAxis(coords.x, width()) || isOutOfBoundsAxis(coords.y, height());
	}

	/**
	 * Tests whether a coordinate is out-of-bounds.
	 */
	private static boolean isOutOfBoundsAxis(int value, int max) {
		return (value < 0) || (value >= max);
	}

	/**
	 * Retrieves a matrix element.
	 * @param x
	 * @param y
	 * @return Element
	 */
	@SuppressWarnings("unchecked")
	public T get(int x, int y) {
		return (T) matrix[x][y];
	}

	/**
	 * Retrieves a matrix element.
	 * @param coords Coordinates
	 * @return Element
	 */
	public T get(Coordinates coords) {
		return get(coords.x, coords.y);
	}

	/**
	 * Sets a matrix element.
	 * @param x
	 * @param y
	 * @param value		Element value
	 */
	public void set(int x, int y, T value) {
		matrix[x][y] = value;
	}

	/**
	 * Sets a matrix element.
	 * @param coords	Coordinates
	 * @param value		Element value
	 */
	public void set(Coordinates coords, T value) {
		set(coords, value);
	}

	/**
	 * Fills this matrix with the given value.
	 * @param value Value
	 */
	public void fill(T value) {
		final int w = width();
		final int h = height();
		for(int x = 0; x < w; ++x) {
			for(int y = 0; y < h; ++y) {
				matrix[x][y] = value;
			}
		}
	}

	/**
	 * Creates a new matrix transformed by the given mapper.
	 * @param mapper 		Element mapper
	 * @param ignore		Whether to ignore <tt>null</tt> elements
	 * @return New matrix
	 * @param <R> Result type
	 */
	@SuppressWarnings("unchecked")
	public <R> Matrix<R> map(Function<T, R> mapper, boolean ignore) {
		final int w = width();
		final int h = height();
		final Matrix<R> result = new Matrix<>(w, h);
		for(int x = 0; x < w; ++x) {
			for(int y = 0; y < h; ++y) {
				final T value = (T) matrix[x][y];
				if(!ignore || (value != null)) {
					result.matrix[x][y] = mapper.apply(value);
				}
			}
		}
		return result;
	}

	/**
	 * Creates a new matrix transformed by the given mapper.
	 * @param mapper Element mapper
	 * @return New matrix
	 * @param <R> Result type
	 */
	public <R> Matrix<R> map(Function<T, R> mapper) {
		return map(mapper, true);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("w", width())
			.append("h", height())
			.build();
	}
}
