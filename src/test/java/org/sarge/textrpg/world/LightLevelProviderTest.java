package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.util.Percentile;

public class LightLevelProviderTest {
	private LightLevelProvider provider;
	private Location loc;

	@BeforeEach
	public void before() {
		// Create current location
		loc = mock(Location.class);
		when(loc.contents()).thenReturn(new Contents());
		when(loc.emission(Emission.LIGHT)).thenReturn(Percentile.ZERO);
		when(loc.terrain()).thenReturn(Terrain.DESERT);

		// Create provider
		provider = new LightLevelProvider();
		provider.setVisibilityThreshold(Percentile.HALF);
	}

	private void daylight() {
		provider.update(new TimePeriod("period", LocalTime.MIN, Percentile.ONE));
	}

	@Test
	public void isPartial() {
		assertEquals(true, provider.isPartial(Percentile.ZERO));
		assertEquals(false, provider.isPartial(Percentile.HALF));
		assertEquals(false, provider.isPartial(Percentile.ONE));
	}

	@Test
	public void dark() {
		assertEquals(false, provider.isDaylight());
		assertEquals(false, provider.isAvailable(loc));
		assertEquals(Percentile.ZERO, provider.level(loc));
	}

	@Test
	public void isDaylight() {
		daylight();
		assertEquals(true, provider.isDaylight());
		assertEquals(true, provider.isAvailable(loc));
		assertEquals(Percentile.ONE, provider.level(loc));
	}

	@Test
	public void terrain() {
		provider.setTerrainModifier(terrain -> Percentile.HALF);
		daylight();
		when(loc.terrain()).thenReturn(Terrain.FOREST);
		assertEquals(true, provider.isAvailable(loc));
		assertEquals(Percentile.HALF, provider.level(loc));
	}

	@Test
	public void terrainDark() {
		when(loc.terrain()).thenReturn(Terrain.DARK);
		assertEquals(false, provider.isAvailable(loc));
		assertEquals(Percentile.ZERO, provider.level(loc));
	}

	@Test
	public void light() {
		when(loc.emission(Emission.LIGHT)).thenReturn(Percentile.HALF);
		assertEquals(true, provider.isAvailable(loc));
		assertEquals(Percentile.HALF, provider.level(loc));
	}
}
