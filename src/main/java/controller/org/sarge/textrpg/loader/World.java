package org.sarge.textrpg.loader;

import org.sarge.textrpg.entity.CharacterEntity;
import org.sarge.textrpg.entity.Race;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.world.Location;

/**
 * World model.
 * @author Sarge
 */
public class World {
	private final Registry<Skill> skills = new Registry<>(Skill::getName);
	private final Registry<ObjectDescriptor> descriptors = new Registry<>(ObjectDescriptor::getName);
	private final Registry<Race> races = new Registry<>(Race::name);
	private final Registry<WorldObject> objects = new Registry<>(WorldObject::name);
	private final Registry<Location> locations = new Registry<>(Location::getName);
	private final Registry<CharacterEntity> chars = new Registry<>(CharacterEntity::name);
	
	/**
	 * @return Skill definitions
	 */
	public Registry<Skill> getSkills() {
		return skills;
	}
	
	/**
	 * @return Object descriptors
	 */
	public Registry<ObjectDescriptor> getDescriptors() {
		return descriptors;
	}

	/**
	 * @return Location registry
	 */
	public Registry<Location> getLocations() {
		return locations;
	}
	
	/**
	 * @return Object registry
	 */
	public Registry<WorldObject> getObjects() {
		return objects;
	}
	
	public Registry<Race> getRaces() {
		return races;
	}

	/**
	 * @return Character registry
	 */
	public Registry<CharacterEntity> getCharacters() {
		return chars;
	}
}
