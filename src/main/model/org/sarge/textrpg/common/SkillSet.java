package org.sarge.textrpg.common;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Set of skills known by an entity.
 * @author Sarge
 */
public interface SkillSet {
	/**
	 * @return Skills in this set
	 */
	Stream<Skill> stream();

	/**
	 * @param skill Skill
	 * @return Whether this set contains the given skill
	 */
	boolean contains(Skill skill);

	/**
	 * Finds the most advanced skill in a <i>group</i>.
	 * @param skill Skill
	 * @return Most advanced skill
	 * @throws IllegalArgumentException if the skill is not present
	 */
	Skill find(Skill skill);

	/**
	 * Mutable implementation.
	 */
	public class MutableSkillSet extends AbstractEqualsObject implements SkillSet {
		private final Set<Skill> skills;

		/**
		 * Default constructor for an empty skills-set.
		 */
		public MutableSkillSet() {
			this(new HashSet<>());
		}

		/**
		 * Copy constructor.
		 * @param skills Skill-set to copy
		 */
		public MutableSkillSet(SkillSet skills) {
			this(skills.stream().collect(toSet()));
		}

		/**
		 * Constructor.
		 * @param skills Skill-set
		 */
		private MutableSkillSet(Set<Skill> skills) {
			this.skills = new StrictSet<>(skills);
		}

		@Override
		public Stream<Skill> stream() {
			return skills.stream();
		}

		@Override
		public boolean contains(Skill skill) {
			return skills.contains(skill);
		}

		@Override
		public Skill find(Skill skill) {
			// Check for optional skill
			if(!contains(skill)) {
				if(skill.isMandatory()) throw new IllegalArgumentException("Skill not present: " + skill);
				return skill.defaultSkill();
			}

			// Otherwise find most advanced skill in the group
			Skill result = skill;
			while(true) {
				// Stop if no more skills in this group
				final var next = result.next();
				if(!next.isPresent()) {
					break;
				}

				// Stop if next skill is not a member
				if(!contains(next.get())) {
					break;
				}

				// Otherwise walk to next skill in group
				result = next.get();
			}
			return result;
		}

		/**
		 * Determines the missing required skills in order to add the given skill to this set.
		 * @param skill Skill to validate
		 * @return Missing skills or empty if the skill is valid to add
		 */
		public Collection<Skill> validate(Skill skill) {
			final Set<Skill> required = new HashSet<>();
			build(skill, required);
			required.removeAll(skills);
			return required;
		}

		/**
		 * Recursively builds the required skills for the given skill.
		 * @param skill			Skill
		 * @param required		Cumulative required skills
		 */
		private static void build(Skill skill, Set<Skill> required) {
			for(Skill req : skill.required()) {
				required.add(req);
				build(req, required);
			}
		}

		/**
		 * Adds a skill to this set.
		 * @param skill Skill to add
		 * @throws IllegalArgumentException if the skill has already been added
		 * @throws IllegalStateException if the skill is not valid to be added
		 * @see #validate(Skill)
		 */
		public void add(Skill skill) {
			if(!validate(skill).isEmpty()) throw new IllegalStateException("Skill requirements not met: " + skill.name());
			skills.add(skill);
		}
	}
}
