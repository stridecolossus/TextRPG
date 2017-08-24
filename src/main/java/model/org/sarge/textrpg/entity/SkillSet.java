package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.textrpg.common.Description;

/**
 * Mutable set of skills.
 * @author Sarge
 * @see Skill
 */
public final class SkillSet {
	private final Map<Skill, Integer> skills;

	/**
	 * Default constructor for an empty set of skills.
	 */
	public SkillSet() {
		this.skills = new StrictMap<>();
	}
	
	/**
	 * Constructor.
	 * @param skills Skills
	 */
	public SkillSet(Map<Skill, Integer> map) {
		this();
		for(Skill skill : map.keySet()) {
			increment(skill, map.get(skill));
		}
	}
	
	/**
	 * Copy constructor.
	 * @param skills Skill-set to copy
	 */
	public SkillSet(SkillSet skills) {
		this.skills = new StrictMap<>(skills.skills);
	}
	
	/**
	 * Convenience constructor for a skill-set with an initial skill.
	 * @param skill Skill
	 * @param level Level
	 */
	public SkillSet(Skill skill, int level) {
		this();
		increment(skill, level);
	}
	
	/**
	 * Looks up the level of the given skill.
	 * @param skill Skill
	 * @return Skill level if present
	 */
	public Optional<Integer> getLevel(Skill skill) {
		return Optional.ofNullable(skills.get(skill));
	}

	/**
	 * @return Skills in this set
	 */
	public Stream<Skill> getSkills() {
		return skills.keySet().stream();
	}

	/**
	 * Modifies a skill level.
	 * @param skill Skill
	 * @param level Level modifier
	 */
	public void increment(Skill skill, int level) {
		skills.merge(skill, level, Integer::sum);
	}
	
	/**
	 * Describes this set of skills.
	 * @return Description
	 */
	public List<Description> describe() {
		final Function<Skill, Description> mapper = skill -> new Description.Builder("info.skills.entry")
			.wrap("name", skill.getName())
			.add("level", skills.get(skill).intValue())
			.build();
		return skills.keySet().stream().map(mapper).collect(toList());
	}
	
	@Override
	public String toString() {
		return skills.toString();
	}
}
