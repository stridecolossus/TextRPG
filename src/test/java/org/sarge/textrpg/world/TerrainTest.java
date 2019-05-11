package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class TerrainTest {
	@Test
	public void uniqueIcons() {
		final Terrain[] terrain = Terrain.values();
		assertEquals(terrain.length, Arrays.stream(terrain).map(Terrain::icon).distinct().count());
	}

	@ParameterizedTest
	@EnumSource(value=Terrain.class, names={"DARK", "UNDERGROUND"})
	public void isDark(Terrain terrain) {
		assertEquals(true, terrain.isDark());
	}

	@ParameterizedTest
	@EnumSource(value=Terrain.class, mode=EnumSource.Mode.EXCLUDE, names={"DARK", "UNDERGROUND"})
	public void isDarkNotDark(Terrain terrain) {
		assertEquals(false, terrain.isDark());
	}

	@ParameterizedTest
	@EnumSource(value=Terrain.class, mode=EnumSource.Mode.EXCLUDE, names={"INDOORS", "DARK", "UNDERGROUND", "WATER"})
	public void isSnowSurface(Terrain terrain) {
		assertEquals(true, terrain.isSnowSurface());
	}

	@ParameterizedTest
	@EnumSource(value=Terrain.class, names={"INDOORS", "DARK", "UNDERGROUND", "WATER"})
	public void isSnowSurfaceNotSnow(Terrain terrain) {
		assertEquals(false, terrain.isSnowSurface());
	}

	@ParameterizedTest
	@EnumSource(Terrain.class)
	public void surfaces(Terrain terrain) {
		assertNotNull(terrain.surfaces());
	}
}
