package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.SkillSet;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.world.Faction;

/**
 * Default implementation than generally delegates to the race of the entity.
 * @author Sarge
 * @see Race
 */
public class DefaultEntityDescriptor extends AbstractEqualsObject implements EntityDescriptor {
	private final Race race;
	private final Optional<Faction> faction;

	/**
	 * Constructor.
	 * @param race 			Race descriptor
	 * @param faction		Optional faction
	 */
	public DefaultEntityDescriptor(Race race, Faction faction) {
		this.race = notNull(race);
		this.faction = Optional.ofNullable(faction);
	}

	@Override
	public Race race() {
		return race;
	}

	@Override
	public String name() {
		return race.name();
	}

	@Override
	public IntegerMap<Attribute> attributes() {
		return race.characteristics().attributes();
	}

	@Override
	public Gender gender() {
		return race.characteristics().gender();
	}

	@Override
	public Alignment alignment() {
		return race.characteristics().alignment();
	}

	@Override
	public Optional<Faction> faction() {
		return faction;
	}

	@Override
	public SkillSet skills() {
		return race.gear().skills();
	}

	@Override
	public Stream<Topic> topics() {
		return Stream.empty();
	}
}
