package org.sarge.textrpg.common;

import static org.junit.Assert.assertEquals;

import java.util.EnumMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.util.DataTableCalculator;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;

public class DataTableCalculatorTest {
	private DataTableCalculator calc;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		// Create terrain data-table
		final Map<Terrain, Number> terrain = new EnumMap<>(Terrain.class);
		terrain.put(Terrain.HILL, 2f);
		
		// Create route-type data-table
		final Map<Route, Number> route = new EnumMap<>(Route.class);
		route.put(Route.LADDER, 3f);
		
		// Create calculator
		calc = new DataTableCalculator(new Map[]{terrain, route});
	}
	
	@Test
	public void multiply() {
		assertEquals(2f * 3f, calc.multiply(Terrain.HILL, Route.LADDER), 0.001f);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void multiplyInvalidLength() {
		calc.multiply(Terrain.HILL);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void multiplyUnknownKey() {
		calc.multiply(Terrain.HILL, Route.TUNNEL);
	}
}
