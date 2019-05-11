package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.common.Effect;
import org.sarge.textrpg.util.EnumerationIntegerMap;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.ValueModifier;
import org.sarge.textrpg.world.Trail;

/**
 * Model that maintains dynamic or transient properties of an entity.
 * @author Sarge
 */
public class EntityModel extends AbstractObject implements ValueModifier.Source {
	/**
	 * Applied effect record.
	 */
	public class AppliedEffect extends AbstractObject {
		private final String name;
		private final ValueModifier mod;
		private final int size;
		private final Effect.Group type;

		/**
		 * Constructor.
		 * @param name			Effect name
		 * @param mutator		Mutator method
		 * @param size			Magnitude
		 * @param group			Effect group
		 */
		AppliedEffect(String name, ValueModifier mod, int size, Effect.Group group) {
			this.name = notEmpty(name);
			this.mod = notNull(mod);
			this.size = oneOrMore(size);
			this.type = notNull(group);
			effects.add(this);
		}

		/**
		 * @return Effect name
		 */
		public String name() {
			return name;
		}

		/**
		 * @return Magnitude
		 */
		public int size() {
			return size;
		}

		/**
		 * @return Effect group
		 */
		public Effect.Group group() {
			return type;
		}

		/**
		 * Removes this effect.
		 */
		public void remove() {
			final boolean removed = effects.remove(this);
			if(removed) {
				mod.modify(-size);
			}
		}
	}

	// Model
	private final EnumerationIntegerMap<Attribute> attrs;
	private final EntityValueIntegerMap values = new EntityValueIntegerMap();
	private final Trail trail;
	private final List<AppliedEffect> effects = new ArrayList<>();

	// State
	private Stance stance = Stance.DEFAULT;
	private Group group = Group.NONE;

	/**
	 * Constructor.
	 * @param name			Name
	 * @param handler 		Entity handler
	 */
	protected EntityModel(String name, IntegerMap<Attribute> attrs) {
		this.attrs = new EnumerationIntegerMap<>(Attribute.class, attrs);
		this.trail = new Trail();
	}

	/**
	 * @return Entity attributes
	 */
	public IntegerMap<Attribute> attributes() {
		return attrs;
	}

	/**
	 * @return Transient entity-values
	 */
	public EntityValueIntegerMap values() {
		return values;
	}

	/**
	 * @return Applied effects
	 */
	public Stream<AppliedEffect> effects() {
		return effects.stream();
	}

	/**
	 * Finds the value-modifier for the given key.
	 * @param key Value modifier key
	 * @return Modifier
	 */
	@Override
	public ValueModifier modifier(ValueModifier.Key key) {
		if(key instanceof EntityValue.Key) {
			return values.get((EntityValue.Key) key);
		}
		else
		if(key instanceof Attribute) {
			return attrs.get((Attribute) key);
		}
		else {
			throw new IllegalArgumentException("Invalid modifier key: " + key);
		}
		// TODO - damage effect? would need to access Entity::damage()
	}

	/**
	 * @return Current stance of this entity
	 */
	public Stance stance() {
		return stance;
	}

	/**
	 * Sets the stance of this entity.
	 * @param stance New stance
	 */
	public void stance(Stance stance) {
		this.stance = notNull(stance);
	}

	/**
	 * @return Group that this entity belongs to
	 */
	public Group group() {
		return group;
	}

	/**
	 * Sets the group that this entity belongs to.
	 * @param group New group
	 */
	void group(Group group) {
		this.group = notNull(group);
	}

	/**
	 * @return Trail
	 */
	public Trail trail() {
		return trail;
	}
}
