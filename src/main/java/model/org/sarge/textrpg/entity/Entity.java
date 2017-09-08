package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.EventHolder;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Hidden;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Tracks;

/**
 * Entity base-class.
 * @author Sarge
 * TODO
 * - mounts: saddle-bags, shoes, general buffs, etc.
 * - horse-vendor rather than lead/abandon
 */
public abstract class Entity extends Thing implements Actor {
	private static final long FORGET_DURATION = Duration.ofMinutes(5).toMillis();

	protected final Race race;

	private final MutableIntegerMap<Attribute> attrs;
	private final MutableIntegerMap<EntityValue> values = new MutableIntegerMap<>(EntityValue.class);
	private final SkillSet skills;
	private final Equipment equipment = new Equipment();
	private final List<AppliedEffect> applied = new ArrayList<>();
	private final LinkedList<Tracks> tracks = new LinkedList<>();

	private final EventQueue queue = new EventQueue();
	private final EventHolder holder = new EventHolder();
	private final EntityManager manager;

	private Stance stance = Stance.DEFAULT;
	private Optional<Group> group = Optional.empty();
	private Percentile vis = Percentile.ONE;
	private Induction induction;

	/**
	 * Constructor.
	 * @param race		Entity race
	 * @param attrs		Initial attributes
	 * @param manager	Manager
	 */
	protected Entity(Race race, IntegerMap<Attribute> attrs, EntityManager manager) {
		Check.notNull(race);
		Check.notNull(manager);
		this.race = race;
		this.attrs = new MutableIntegerMap<>(Attribute.class, attrs);
		this.skills = new SkillSet(race.equipment().skills());
		this.manager = manager;
	}

	@Override
	public String name() {
		return race.name();
	}

	/**
	 * @return Event queue for this entity
	 */
	@Override
	public EventQueue queue() {
		return queue;
	}

	/**
	 * @return Event holder for the current AI action
	 */
	public EventHolder actionEventHolder() {
		return holder;
	}

	/**
	 * @return Entity manager
	 */
	public EntityManager manager() {
		return manager;
	}

	/**
	 * @return Notification handler
	 */
	public abstract Notification.Handler handler();

	/**
	 * @return Race of this entity
	 */
	public Race race() {
		return race;
	}

	@Override
	public Size size() {
		return race.attributes().size();
	}

	/**
	 * @return Gender of this entity
	 */
	public abstract Gender gender();

	/**
	 * @return Alignment of this entity
	 */
	public abstract Alignment alignment();

	@Override
	public Percentile visibility() {
		if(vis == null) {
			// TODO - built-in hidden?
			return Percentile.ONE;
		}
		else {
			return vis;
		}
	}

	@Override
	public boolean isSentient() {
		return true;
	}

	@Override
	public long forgetPeriod() {
		return FORGET_DURATION;
	}

	/**
	 * Sets the visibility of this entity.
	 * @param vis Visibility or <tt>null</tt> for default for this entity
	 */
	void setVisibility(Percentile vis) {
		this.vis = vis;
	}

	@Override
	public Optional<Emission> emission(Emission.Type type) {
		return equipment.stream().map(obj -> obj.emission(type)).findFirst().filter(Optional::isPresent).map(Optional::get);
	}

	@Override
	public final int weight() {
		// TODO
		return 42/*weight*/ + contents().getWeight();
	}

	/**
	 * @return Whether this entity can enter water locations
	 */
	public boolean isSwimming() {
		return false;
	}

	/**
	 * @return Entity description key
	 */
	protected abstract String getDescriptionKey();

	@Override
	public Description describe() {
		final Description.Builder builder = new Description.Builder("description." + getDescriptionKey());
		builder.wrap("name", name());
		builder.wrap("action", "entity." + stance); // TODO - custom actions
		// TODO - wrap commas, builder.wrap("wielding", equipment.get(DeploymentSlot.MAIN_HAND)); // TODO - "held" and "wielding", i.e. holding lantern
		// TODO - current action/induction/emote
		return builder.build();
	}

	/**
	 * @return Current location of this entity
	 */
	public Location location() {
		return (Location) super.root();
	}

	/**
	 * @return Group containing this entity (if any)
	 */
	public Optional<Group> group() {
		return group;
	}

	/**
	 * Sets the group of this entity.
	 * @param group New group or <tt>null</tt> to remove
	 */
	void setGroup(Group group) {
		this.group = Optional.ofNullable(group);
	}

	/**
	 * @return Attributes of this entity
	 */
	public IntegerMap<Attribute> attributes() {
		return attrs;
	}

	/**
	 * Modifies an attribute value.
	 * @param attr		Attribute to modify
	 * @param value		Amount
	 */
	void modify(Attribute attr, int value) {
		attrs.add(attr, value);
	}

	/**
	 * @return Values of this entity
	 */
	public IntegerMap<EntityValue> values() {
		return values;
	}

	/**
	 * Modifies an entity-value.
	 * @param value		Value to modify
	 * @param amount	Amount
	 */
	public void modify(EntityValue value, int amount) {
		values.add(value, amount); // TODO - addClamp()

		if(values.get(value) < 0) {
			values.set(value, 0);
		}
	}

	@Override
	public boolean isDead() {
		return values.get(EntityValue.HEALTH) <= 0;
	}

	@Override
	protected void damage(DamageType type, int amount) {
		// Apply damage
		// TODO - resistances
		values.add(EntityValue.HEALTH, -amount);

		// Destroy this entity if dead
		if(isDead()) {
			// TODO - notify entity, group, location
			destroy();
		}
	}

	/**
	 * @return Skill-set
	 */
	public SkillSet skills() {
		return skills;
	}

	/**
	 * Looks up the level for the given skill if any.
	 * @param skill Skill to lookup
	 * @return Skill level
	 */
	public Optional<Integer> skillLevel(Skill skill) {
		return skills.getLevel(skill);
	}

	/**
	 * @return Stance of this entity
	 */
	public Stance stance() {
		return stance;
	}

	/**
	 * Changes the stance of this entity.
	 * @param stance New stance
	 * @throws ActionException if the stance is already set or is not a valid transition
	 * @see Stance#isValidTransition(Stance)
	 * TODO - public
	 */
	public void setStance(Stance stance) throws ActionException {
		Check.notNull(stance);
		this.stance = stance;
		setVisibility(null);
	}

	@Override
	public Contents contents() {
		return Contents.EMPTY;
	}

	/**
	 * @return Equipment being worn by this entity
	 */
	public Equipment equipment() {
		return equipment;
	}

	/**
	 * @return Currently equipped weapon
	 * @see Race#weapon()
	 */
	public Weapon weapon() {
		return equipment.weapon().orElse(race.equipment().weapon());
	}

	@Override
	public boolean perceives(Hidden obj) {
		// Short-cut tests
		if(Percentile.ONE.equals(obj.visibility())) return true;
		if(Percentile.ZERO.equals(obj.visibility())) return false;

		// Passive perception test
		// TODO - modifier
		final int perception = attrs.get(Attribute.PERCEPTION);
		if((perception * 3) > obj.visibility().invert().intValue()) {
			return true;
		}

		// Check group members
		if(group.isPresent()) {
			final boolean result = group.get().members().anyMatch(e -> e.perceives(obj));
			if(result) return true;
		}

		// Not perceived
		return false;
	}

	/**
	 * @return Conversation topics of this entity
	 */
	public Stream<Topic> topics() {
		return Stream.empty();
	}

	/**
	 * @return Tracks generated by this entity
	 */
	public Stream<Tracks> tracks() {
		return tracks.stream();
	}

	/**
	 * Adds a new set of tracks for this entity.
	 * @param tracks	Tracks
	 * @param max		Age of tracks to remove
	 */
	protected void add(Tracks tracks, long max) {
		// Remove old tracks
		final long time = tracks.getCreationTime() - max;
		final Collection<Tracks> old = this.tracks.stream().filter(t -> t.getCreationTime() < time).collect(toList());
		old.stream().forEach(Tracks::remove);
		this.tracks.removeAll(old);

		// Add new tracks
		this.tracks.add(tracks);
	}

	/**
	 * @param e Entity
	 * @return Whether this entity is following the given entity
	 */
	public boolean isFollowing(Entity e) {
		return false;
	}

	/**
	 * @return Followers of this entity (default is empty)
	 */
	public Stream<Entity> followers() {
		return Stream.empty();
	}

	/**
	 * Follows the given entity.
	 * @param e Entity to follow or <tt>null</tt> to stop following
	 * @throws ActionException if this entity cannot follow
	 * @throws IllegalArgumentException by default
	 */
	protected void follow(Entity e) throws ActionException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Transient effect applied to this entity.
	 */
	public final class AppliedEffect {
		private final EffectMethod effect;
		private final int size;

		/**
		 * Constructor.
		 * @param effect	Effect
		 * @param size		Magnitude
		 */
		private AppliedEffect(EffectMethod effect, int size) {
			this.effect = effect;
			this.size = size;
		}

		/**
		 * @return Effect method
		 */
		public EffectMethod effect() {
			return effect;
		}

		/**
		 * @return Effect magnitude
		 */
		public int size() {
			return size;
		}

		/**
		 * Removes this applied effect.
		 */
		protected void remove() {
			effect.apply(Entity.this, -size);
		}

		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}

	/**
	 * @return Transient effect applied to this entity
	 */
	public Stream<AppliedEffect> getAppliedEffects() {
		return applied.stream();
	}

	/**
	 * Applies the given effect to this entry and registers an expiry event.
	 * @param effect		Effect
	 * @param size			Magnitude
	 * @param duration		Duration for a transient effect
	 * @param queue			Event queue for expiry events
	 */
	protected void apply(EffectMethod effect, int size, Optional<Integer> duration, EventQueue queue) {
		// Apply effect
		effect.apply(this, size);

		// Register applied effect
		final AppliedEffect e = new AppliedEffect(effect, size);
		this.applied.add(e);

		// Create expiry event for transient effects
		duration.ifPresent(t -> {
			final Runnable expiry = () -> {
				e.remove();
				this.applied.remove(e);
			};
			queue.add(expiry, t);
		});
	}

	/**
	 * Dispels all transient effects.
	 */
	void dispel() {
		applied.stream().filter(e -> !e.effect().isWound()).forEach(AppliedEffect::remove);
		applied.clear();
	}

	/**
	 * @return Active induction if any
	 */
	public Induction getInduction() {
		return induction;
	}

	/**
	 * Starts an induction.
	 * @param induction		Induction call-back
	 * @param duration		Duration of this induction (ms)
	 * @throws IllegalStateException if an induction is already active
	 */
	public void start(Induction induction, long duration, boolean repeat) {
		Check.oneOrMore(duration);
		if(this.induction != null) throw new IllegalStateException("Induction already started");
		this.induction = induction;

		// Add completion event
		final Runnable event = () -> {
			// Ignore interrupted inductions
			if(this.induction == null) return;

			// Complete induction
			try {
				final Description description = induction.complete();
				if(description != null) {
					alert(new Message(description));
				}
			}
			catch(ActionException e) {
				alert(Message.of(e));
			}

			// Stop if induction is finished
			if(!repeat) {
				this.induction = null;
			}
		};
		queue.add(event, duration, repeat);
	}

	/**
	 * Interrupts the active induction.
	 * @throws IllegalStateException if no induction is active
	 */
	public void interrupt() {
		if(induction == null) throw new IllegalStateException("Induction not started");
		induction.interrupt();
		induction = null;
	}

	@Override
	public void alert(Notification n) {
		n.accept(handler());
	}

	@Override
	protected void destroy() {
		induction = null;
		holder.cancel();
		queue.reset();
		values.set(EntityValue.HEALTH, 0);
		super.destroy();
	}
}
