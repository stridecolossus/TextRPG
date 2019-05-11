package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Location;

/**
 * Starter area descriptor.
 * @author Sarge
 */
public final class StarterArea extends AbstractEqualsObject {
	private final Location loc;
	private final Faction faction;
	private final Race race;

	/**
	 * Constructor.
	 * @param loc			Start location
	 * @param faction		Primary faction
	 * @param race			Race
	 */
	public StarterArea(Location loc, Faction faction, Race race) {
		this.loc = notNull(loc);
		this.faction = notNull(faction);
		this.race = notNull(race);
	}

	/**
	 * @return Start location
	 */
	public Location location() {
		return loc;
	}

	/**
	 * @return Primary faction
	 */
	public Faction faction() {
		return faction;
	}

	/**
	 * @return Race
	 */
	public Race race() {
		return race;
	}
}
