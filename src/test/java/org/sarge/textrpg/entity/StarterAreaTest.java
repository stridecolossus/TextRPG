package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Location;

public class StarterAreaTest {
	@Test
	public void constructor() {
		final Location loc = mock(Location.class);
		final Faction faction = mock(Faction.class);
		final Race race = new Race.Builder("race").build();
		final StarterArea starter = new StarterArea(loc, faction, race);
		assertEquals(loc, starter.location());
		assertEquals(faction, starter.faction());
		assertEquals(race, starter.race());
	}
}
