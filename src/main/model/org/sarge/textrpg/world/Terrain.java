package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.util.TextHelper;

/**
 * Terrain types.
 * @author Sarge
 */
public enum Terrain {
	GRASSLAND('-'),
	FARMLAND('$'),
	SCRUBLAND('&'),
	PLAINS('_'),
	HILL('('),
	MOUNTAIN('<'),
	FOREST('#'),
	WOODLAND('+'),
	JUNGLE('?'),
	MARSH('@'),
	DESERT('%'),
	ICE('*'),
	URBAN('['),
	INDOORS('{'),
	DARK('!'),
	UNDERGROUND('^'),
	WATER('~');

	/**
	 * Checks terrain icons are unique.
	 */
	static {
		assert Arrays.stream(Terrain.values()).map(Terrain::icon).distinct().count() == Terrain.values().length;
	}

	/**
	 * Converter.
	 */
	public static final Converter<Terrain> CONVERTER = Converter.enumeration(Terrain.class);

	/**
	 * Default surfaces.
	 */
	private static final List<String> INDOOR = surfaces("floor", "ceiling", "walls");

	/**
	 * Default surfaces.
	 */
	private static final List<String> OUTDOOR = surfaces("ground", "sky");

	/**
	 * Helper - Builds a list of surfaces.
	 */
	private static List<String> surfaces(String... name) {
		return Arrays.stream(name).map(str -> TextHelper.join("surface", str)).collect(toList());
	}

	private final char icon;

	/**
	 * Constructor.
	 * @param icon Display icon
	 */
	private Terrain(char icon) {
		this.icon = icon;
	}

	/**
	 * @return Display icon
	 */
	public char icon() {
		return icon;
	}

	/**
	 * @return Whether this terrain is naturally dark irrespective of the time-of-day
	 */
	public boolean isDark() {
		switch(this) {
		case UNDERGROUND:
		case DARK:
			return true;

		default:
			return false;
		}
	}

	/**
	 * @return Whether snow settles on this terrain
	 */
	public boolean isSnowSurface() {
		switch(this) {
		case INDOORS:
		case DARK:
		case UNDERGROUND:
		case WATER:
			return false;

		default:
			return true;
		}
	}

	/**
	 * @return Surfaces for this terrain
	 */
	public Stream<String> surfaces() {
		switch(this) {
		case INDOORS:
		case DARK:
		case UNDERGROUND:
			return INDOOR.stream();

		default:
			return OUTDOOR.stream();
		}
	}
}
