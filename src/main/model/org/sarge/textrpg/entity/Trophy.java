package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.MutableIntegerMap;

/**
 * Record of killed enemies.
 * @author Sarge
 */
public class Trophy extends AbstractEqualsObject {
	private final MutableIntegerMap<Race> trophy = new MutableIntegerMap<>();

	/**
	 * @param race Race
	 * @return Number of kills for the given race
	 */
	public int count(Race race) {
		return trophy.get(race).get();
	}

	/**
	 * Describes this trophy.
	 * @return Trophy
	 */
	public List<Description> describe() {
		return trophy.keys().map(this::describe).collect(toList());
	}

	/**
	 * Describes a trophy entry.
	 */
	private Description describe(Race race) {
		return new Description.Builder("trophy.entry")
			.name(race.name())
			.add("count", count(race))
			.build();
	}

	/**
	 * Registers a kill.
	 * @param race Race
	 */
	protected void add(Race race) {
		trophy.get(race).modify(1);
	}
}
