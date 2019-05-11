package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Location;

public class StarterAreaLoaderTest {
	private StarterAreaLoader loader;
	private Location loc;
	private Race race;
	private Faction faction;


	@BeforeEach
	public void before() {
		// Register race
		race = new Race.Builder("race").build();
		final Registry<Race> races = new Registry.Builder<>(Race::name).add(race).build();

		// Register faction
		faction = mock(Faction.class);
		when(faction.name()).thenReturn("faction");
		final Registry<Faction> factions = new Registry.Builder<>(Faction::name).add(faction).build();

		// Add location
		final Location.Linker linker = mock(Location.Linker.class);
		loc = mock(Location.class);
		when(linker.connector("loc")).thenReturn(loc);

		// Create loader
		loader = new StarterAreaLoader(linker, races, factions);
	}

	@Test
	public void load() throws IOException {
		// Load starter areas
		final var starters = loader.load(new BufferedReader(new StringReader("loc faction race")));
		assertNotNull(starters);
		assertEquals(1, starters.size());

		// Check starter area
		final StarterArea area = starters.iterator().next();
		final StarterArea expected = new StarterArea(loc, faction, race);
		assertEquals(expected, area);
	}
}
