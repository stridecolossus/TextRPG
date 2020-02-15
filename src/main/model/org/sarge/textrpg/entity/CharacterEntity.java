package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;
import java.util.stream.Stream;

import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.SkillSet;
import org.sarge.textrpg.world.Faction;

/**
 * Character entity.
 * @author Sarge
 */
public class CharacterEntity extends Entity {
	/**
	 * Entity descriptor for a character.
	 */
	public static class CharacterEntityDescriptor extends DefaultEntityDescriptor {
		private final String name;
		private final Gender gender;
		private final Alignment alignment;
		private final SkillSet skills;
		private final Set<Topic> topics;

		/**
		 * Constructor.
		 * @param race				Race
		 * @param name				Name
		 * @param gender			Gender
		 * @param alignment			Alignment
		 * @param faction			Optional faction
		 * @param skills			Additional skills
		 * @param topics			Conversation topics
		 */
		public CharacterEntityDescriptor(Race race, String name, Gender gender, Alignment alignment, Faction faction, SkillSet skills, Set<Topic> topics) {
			super(race, faction);
			this.name = notEmpty(name);
			this.gender = notNull(gender);
			this.alignment = notNull(alignment);
			this.skills = notNull(skills);
			this.topics = Set.copyOf(topics);
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public Gender gender() {
			return gender;
		}

		@Override
		public Alignment alignment() {
			return alignment;
		}

		@Override
		public SkillSet skills() {
			return skills;
		}

		@Override
		public Stream<Topic> topics() {
			return topics.stream();
		}
	}

	private final FollowModel follow = new FollowModel() {
		@Override
		protected boolean isLeader() {
			return true;
		}
	};

	private MovementMode mode = super.movement(); // TODO - urgh

	/**
	 * Constructor.
	 * @param descriptor		Character descriptor
	 * @param handler			Handler
	 */
	public CharacterEntity(EntityDescriptor descriptor, EntityManager handler) {
		super(descriptor, handler);
	}

	@Override
	public FollowModel follower() {
		return follow;
	}

	@Override
	public MovementMode movement() {
		if(mode == null) {
			return super.movement();
		}
		else {
			return mode;
		}
	}

	/**
	 * Sets the movement mode of this entity.
	 * @param mode Movement mode or <tt>null</tt> for default mode
	 */
	void movement(MovementMode mode) {
		this.mode = mode;
	}
}
