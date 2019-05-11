
package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.SkillSet;
import org.sarge.textrpg.common.SkillSet.MutableSkillSet;
import org.sarge.textrpg.common.TransientModel;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.PlayerSettings.Setting;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.EnumerationIntegerMap;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Persistent;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Faction.Association;

/**
 * Player-character.
 * @author Sarge
 */
public class PlayerCharacter extends CharacterEntity {
	/**
	 * Entity descriptor for a player.
	 */
	public static class PlayerEntityDescriptor extends DefaultEntityDescriptor implements Persistent {
		private final String name;
		private final IntegerMap<Attribute> attrs;
		private final Gender gender;
		private final Alignment alignment;

		/**
		 * Constructor.
		 * @param race				Race
		 * @param name				Name
		 * @param gender			Gender
		 * @param alignment			Alignment
		 * @param faction			Primary faction
		 */
		public PlayerEntityDescriptor(Race race, String name, IntegerMap<Attribute> attrs, Gender gender, Alignment alignment, Faction faction) {
			super(race, faction);
			this.name = notEmpty(name);
			this.attrs = notNull(attrs);
			this.gender = notNull(gender);
			this.alignment = notNull(alignment);
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public IntegerMap<Attribute> attributes() {
			return attrs;
		}

		@Override
		public Gender gender() {
			return gender;
		}

		@Override
		public Alignment alignment() {
			return alignment;
		}

		/**
		 * Builder for a player descriptor.
		 */
		public static class Builder {
			private Race race;
			private String name;
			private Gender gender;
			private Alignment alignment = Alignment.NEUTRAL;
			private Faction faction;
			private EnumerationIntegerMap<Attribute> attrs = new EnumerationIntegerMap<>(Attribute.class);

			/**
			 * Sets the name of this player.
			 * @param name Player name
			 */
			public Builder name(String name) {
				this.name = name;
				return this;
			}

			/**
			 * Sets the race of this player.
			 * @param race Race
			 */
			public Builder race(Race race) {
				this.race = race;
				return this;
			}

			/**
			 * Sets the gender of this player.
			 * @param gender Gender
			 */
			public Builder gender(Gender gender) {
				this.gender = gender;
				return this;
			}

			/**
			 * Sets the alignment of this player.
			 * @param alignment Alignment
			 */
			public Builder alignment(Alignment alignment) {
				this.alignment = alignment;
				return this;
			}

			/**
			 * Sets the initial faction of this player.
			 * @param faction Faction
			 */
			public Builder faction(Faction faction) {
				this.faction = faction;
				return this;
			}

			/**
			 * Sets an attribute of this player.
			 * @param attr		Attribute
			 * @param value		Value
			 */
			public Builder attribute(Attribute attr, int value) {
				attrs.get(attr).set(value);
				return this;
			}

			/**
			 * Constructs a new descriptor.
			 * @return New descriptor
			 * @throws IllegalArgumentException if any descriptor property has not been set
			 */
			public PlayerEntityDescriptor build() {
				return new PlayerEntityDescriptor(race, name, attrs, gender, alignment, faction);
			}
		}
	}

	/**
	 * Persistent entity model.
	 */
	public static class PlayerModel extends AbstractObject implements Persistent {
		private final Inventory inv;
		private final MutableSkillSet skills;
		private final Map<Faction, Relationship> associations;
		private final Trophy trophy;
		private final PlayerSettings settings;
		private final RecipeModel recipes;

		/**
		 * Constructor.
		 * @param inv				Inventory
		 * @param skills			Skills
		 * @param associations		Faction associations
		 * @param trophy			Trophy
		 * @param settings			Player settings and preferences
		 * @param recipes			Recipes known by this player
		 */
		public PlayerModel(Inventory inv, SkillSet skills, Map<Faction, Relationship> associations, Trophy trophy, PlayerSettings settings, RecipeModel recipes) {
			this.inv = notNull(inv);
			this.skills = new MutableSkillSet(skills);
			this.associations = new StrictMap<>(associations);
			this.trophy = notNull(trophy);
			this.settings = notNull(settings);
			this.recipes = notNull(recipes);
		}

		/**
		 * Convenience constructor for a new player.
		 * @param faction Starting faction association
		 */
		public PlayerModel(Faction.Association association) {
			this(new Inventory(), new MutableSkillSet(), new StrictMap<>(), new Trophy(), new PlayerSettings(), new RecipeModel());
			add(association);
		}

		/**
		 * @return Trophy model
		 */
		public Trophy trophy() {
			return trophy;
		}

		/**
		 * @return Skills-set
		 */
		public MutableSkillSet skills() {
			return skills;
		}

		/**
		 * @return Factions relationships
		 */
		public Stream<Faction.Association> associations() {
			return associations.entrySet().stream().map(e -> new Faction.Association(e.getKey(), e.getValue()));
		}

		/**
		 * Sets the association of this player with the given faction.
		 * @param association Faction association
		 */
		void add(Faction.Association association) {
			associations.put(association.faction(), association.relationship());
		}

		/**
		 * @return Recipes known by this player
		 */
		public RecipeModel recipes() {
			return recipes;
		}
	}

	/**
	 * Player notification handler that generally delegates to {@link Entity#alert(Description)}.
	 */
	private static class PlayerNotificationHandler implements Notification.Handler {
		@Override
		public void init(Entity actor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void handle(MovementNotification move, Entity actor) {
			write(move, actor);
		}

		@Override
		public void handle(EmissionNotification emission, Entity actor) {
			// TODO - perception check
			write(emission, actor);
		}

		@Override
		public void handle(CombatNotification combat, Entity actor) {
			write(combat, actor);
		}

		@Override
		public void light(Entity actor) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Alerts the player of the given notification.
		 * @param notification		Notification
		 * @param actor				Actor
		 */
		private static void write(Notification notification, Entity actor) {
			actor.alert(notification.describe());
		}
	}

	private final PlayerModel model;
	private final TransientModel hidden;

	private Thing prev;

	/**
	 * Constructor.
	 * @param descriptor		Player-character descriptor
	 * @param queue				Queue for player events
	 * @param listener			Listener for induction responses
	 * @param model				Persistent model
	 */
	public PlayerCharacter(PlayerEntityDescriptor descriptor, Event.Queue queue, Consumer<Response> listener, PlayerModel model) {
		super(descriptor, new EntityManager(queue, new PlayerNotificationHandler(), listener));
		this.model = notNull(model);
		this.hidden = new TransientModel(queue);
	}

	@Override
	public Inventory contents() {
		return model.inv;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public boolean isSwimEnabled() {
		return model.settings.toBoolean(Setting.SWIM);
	}

	@Override
	public Percentile emission(Emission emission) {
		return model.inv.equipment().emission(emission);
	}

	@Override
	public SkillSet skills() {
		return model.skills;
	}

	@Override
	public boolean perceives(Hidden obj) {
		if(hidden.contains(obj)) {
			return true;
		}
		else {
			return super.perceives(obj);
		}
	}

	/**
	 * @return Persistent player model
	 */
	public PlayerModel player() {
		return model;
	}

	/**
	 * @return Player settings and preferences
	 */
	public PlayerSettings settings() {
		return model.settings;
	}

	/**
	 * @return Hidden objects known by this player
	 */
	public TransientModel hidden() {
		return hidden;
	}

	/**
	 * @return Previous (or <i>it</i>) object or <tt>null</tt> if none
	 */
	public Thing previous() {
		return prev;
	}

	/**
	 * Sets the previous (or <i>it</i>) object.
	 * @param prev Previous object or <tt>null</tt> if none
	 */
	public void setPrevious(Thing prev) {
		this.prev = prev;
	}

	@Override
	public boolean isAssociated(Association association) {
		final Faction faction = association.faction();
		final Relationship def = faction.relationship(this.descriptor().alignment());
		final Relationship relationship = model.associations.getOrDefault(association.faction(), def);
		return relationship.compareTo(association.relationship()) >= 0;
	}

//	// TODO
//	public void add(Location loc, LocationTrigger trigger) {
//		final LocationTrigger.Model model = new LocationTrigger.Model();
//		model.add(loc, trigger);
//		group.add(model);
//	}

	@Override
	public void alert(Description alert) {
		if(alert == Description.DISPLAY_LOCATION) {
			// TODO
		}
		else {
			//super.manager().induction().listener().accept(t);


			// TODO
			// write(n);
			System.out.println("ALERT: "+alert);
		}
	}

	@Override
	public boolean notify(ContentStateChange notification) {
		switch(notification.type()) {
		case LIGHT:
			contents().equipment().update();
			return false;

		default:
			this.alert(notification.describe());
			return false;
		}
	}
}
