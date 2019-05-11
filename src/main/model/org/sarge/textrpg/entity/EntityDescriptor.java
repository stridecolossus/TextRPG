package org.sarge.textrpg.entity;

import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.SkillSet;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.world.Faction;

/**
 * Descriptor for the static properties of an entity.
 * @author Sarge
 */
public interface EntityDescriptor {
	/**
	 * @return Race of this entity
	 */
	Race race();

	/**
	 * @return Name
	 */
	String name();

	/**
	 * @return Attributes
	 */
	IntegerMap<Attribute> attributes();

	/**
	 * @return Gender
	 */
	Gender gender();

	/**
	 * @return Alignment
	 */
	Alignment alignment();

	/**
	 * @return Faction of this entity
	 */
	Optional<Faction> faction();

	/**
	 * @return Skills possessed by this entity
	 */
	SkillSet skills();

	/**
	 * @return Discussion topics
	 */
	Stream<Topic> topics();
}
