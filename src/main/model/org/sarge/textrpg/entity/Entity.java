package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.SkillSet;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Trail;

/**
 * Default entity.
 * @author Sarge
 */
public class Entity extends Thing implements Actor, Parent, Follower, Leader {
	/**
	 * Location trigger activated by an entity.
	 */
	@FunctionalInterface
	public interface LocationTrigger {
		/**
		 * Activates this trigger for the given actor.
		 * @param actor Actor
		 */
		void trigger(Entity actor);
	}

	/**
	 * Default movement mode for a walking entity.
	 */
	protected class DefaultMovementMode implements MovementMode {
		@Override
		public Thing mover() {
			return Entity.this;
		}

		@Override
		public List<Transaction> transactions(int cost) {
			// TODO - better as percentile
			final int actual = cost - model.values().get(EntityValue.MOVEMENT_COST.key()).get();
			if(actual < 0) {
				return List.of();
			}
			else {
				return List.of(MovementMode.transaction(Entity.this, actual, "move.insufficient.stamina"));
			}
		}

		@Override
		public void move(Exit exit) throws ActionException {
			parent(exit.destination());
		}

		@Override
		public Percentile noise() {
			return emission(Emission.SOUND);
		}

		@Override
		public Trail trail() {
			return model.trail();
		}

		@Override
		public Percentile tracks() {
			return descriptor.race().gear().tracks();
		}
	}

	private final EntityDescriptor descriptor;
	private final EntityModel model;
	private final EntityManager manager;
	private final MovementMode mode = new DefaultMovementMode();

	/**
	 * Constructor.
	 * @param descriptor		Entity descriptor
	 * @param manager			Entity manager
	 */
	public Entity(EntityDescriptor descriptor, EntityManager manager) {
		this.descriptor = notNull(descriptor);
		this.model = new EntityModel(descriptor.name(), descriptor.attributes());
		this.manager = notNull(manager);
	}

	/**
	 * @return Entity descriptor
	 */
	public EntityDescriptor descriptor() {
		return descriptor;
	}

	/**
	 * @return Entity model
	 */
	public EntityModel model() {
		return model;
	}

	/**
	 * @return Entity manager
	 */
	public EntityManager manager() {
		return manager;
	}

	@Override
	public String name() {
		return descriptor.name();
	}

	@Override
	public Size size() {
		return descriptor.race().characteristics().size();
	}

	@Override
	public int weight() {
		return contents().weight() + descriptor.race().characteristics().weight();
	}

	@Override
	public final boolean isSentient() {
		return true;
	}

	@Override
	public boolean isRaceCategory(String cat) {
		return descriptor.race().characteristics().isCategory(cat);
	}

	@Override
	public Percentile visibility() {
		return model.values().visibility().get();
	}

	@Override
	public Percentile emission(Emission emission) {
		return contents().equipment().emission(emission);
	}

	@Override
	public boolean isAlive() {
		return model.values().get(EntityValue.HEALTH.key()).get() > 0;
	}

	@Override
	protected void damage(Damage.Type type, int amount) {
		// TODO - resistances?
		model.values().get(EntityValue.HEALTH.key()).modify(-amount);
	}

	@Override
	public Inventory contents() {
		return Inventory.EMPTY;
	}

	/**
	 * @return Movement mode of this entity
	 */
	public MovementMode movement() {
		return mode;
	}

	@Override
	public SkillSet skills() {
		return descriptor.skills();
	}

	/**
	 * Tests whether this entity has the given faction association.
	 * @param association Faction association
	 * @return Whether associated with the given faction (default is <tt>false</tt>)
	 */
	public boolean isAssociated(Faction.Association association) {
		return false;
	}

	/**
	 * @return Whether this entity is a player
	 */
	public boolean isPlayer() {
		return false;
	}

	/**
	 * @return Whether this entity can swim
	 */
	public boolean isSwimEnabled() {
		return false;
	}

	/**
	 * @return Current location of this entity
	 */
	public Location location() {
		Parent p = this.parent();
		while(true) {
			if(p == Parent.LIMBO) return null;
			if(p.parent() == null) return (Location) p;
			p = p.parent();
		}
	}

	@Override
	public FollowerModel follower() {
		throw new UnsupportedOperationException();
	}

	@Override
	public LeaderModel leader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean perceives(Hidden hidden) {
		// Ignore self
		if(hidden == this) return false;

		// Short-cut tests
		final Percentile vis = hidden.visibility();
		if(Percentile.ZERO.equals(vis)) return false;
		if(Percentile.ONE.equals(vis)) return true;
// TODO - perception score
//		if(Percentile.ZERO.equals(perception)) return false;

//		// Passive perception test
//		if(vis.isLessThan(perception)) return true;

		// Test whether perceived by group
		if(model.group().perceives(this, hidden)) return true;

		// Not perceived
		return false;
	}

	/**
	 * @param entity
	 * @return Whether the given entity is a valid target for this entity
	 */
	public boolean isValidTarget(Entity entity) {
		if(entity == this) return false;
		if(model.group() == entity.model().group()) return false;
		if(!descriptor.alignment().isValidTarget(entity.descriptor().alignment())) return false;
		return true;
	}

	@Override
	public void alert(Description alert) {
		// Ignored
	}

	@Override
	public Description describe(ArgumentFormatter.Registry formatters) {
		return new Description.Builder("entity.description")
			.name(this.name())
			.add("stance", model.stance())
			.build();
	}

	@Override
	public boolean notify(ContentStateChange notification) {
		if(notification == ContentStateChange.LIGHT_MODIFIED) {
			manager.handler().light(this);
		}
		return true;
	}

	@Override
	protected void destroy() {
		model.trail().clear();
		manager.queue().remove();
		super.destroy();
	}
}
