package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;

import org.sarge.textrpg.util.DataTable;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.WorldLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Loader for starter areas.
 * @author Sarge
 */
@Service
@ConditionalOnBean(WorldLoader.class)
public class StarterAreaLoader {
	private final Location.Linker linker;
	private final Registry<Race> races;
	private final Registry<Faction> factions;

	/**
	 * Constructor.
	 * @param loader		Location linker
	 * @param races			Races registry
	 * @param factions		Factions registry
	 */
	public StarterAreaLoader(Location.Linker linker, Registry<Race> races, Registry<Faction> factions) {
		this.linker = notNull(linker);
		this.races = notNull(races);
		this.factions = notNull(factions);
	}

	/**
	 * Loads starter areas.
	 * @param r Reader
	 * @return Starter areas
	 * @throws IOException if the starter areas cannot be loaded
	 */
	public Set<StarterArea> load(BufferedReader r) throws IOException {
		return DataTable.load(r).map(this::load).collect(toSet());
	}

	/**
	 * Loads a starter area.
	 * @param row Row
	 * @return Starter area
	 */
	private StarterArea load(String[] row) {
		final Location loc = linker.connector(row[0]);
		final Faction faction = factions.get(row[1]);
		final Race race = races.get(row[2]);
		return new StarterArea(loc, faction, race);
	}
}
