package org.sarge.textrpg.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Value;

/**
 * Effect on an {@link Entity} caused by a spell, trap, weapon, potion, etc.
 * @author Sarge
 */
public final class Effect {
	/**
	 * Effect that does nothing.
	 */
	public static final Descriptor NONE = new Descriptor("none", Collections.emptyList());

	/**
	 * Descriptor for an effect.
	 */
	public static final class Descriptor {
		private final String name;
		private final List<Effect> effects;

		/**
		 * Constructor.
		 * @param name			Effect name
		 * @param effects		Effect(s)
		 */
		public Descriptor(String name, List<Effect> effects) {
			Check.notEmpty(name);
			this.name = name;
			this.effects = new ArrayList<>(effects);
		}

		/**
		 * @return Effect name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return Effect(s)
		 */
		public Stream<Effect> getEffects() {
			return effects.stream();
		}

		/**
		 * Applies this effect to the given entity.
		 * @param targets	Target(s)
		 * @param actor		Actor
		 */
		public void apply(Collection<Entity> targets, Actor actor) {
			for(Effect effect : effects) {
				// Calculate effect magnitude and duration
				final Optional<Integer> duration = effect.duration.map(t -> t.evaluate(actor));
				final int size = effect.size.evaluate(actor);

				// Apply effect to targets
				final EffectMethod method = effect.getMethod();
				for(Entity e : targets) {
					e.apply(method, size, duration, actor.queue());
				}
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private final EffectMethod effect;
	private final Value size;
	private final Optional<Value> duration;

	/**
	 * Constructor.
	 * @param effect		Effect method
	 * @param size			Size of effect
	 * @param duration		Duration of effect or <tt>null</tt> if fixed or permanent
	 * @throws IllegalArgumentException for a {@link DamageEffect} that is not fixed
	 */
	public Effect(EffectMethod effect, Value size, Value duration) {
		// TODO - repeating effect
		Check.notNull(effect);
		Check.notNull(size);
		if((effect instanceof DamageEffect) && (duration != null)) throw new IllegalArgumentException("Damage effect must be permanent");
		// TODO - how to check for damage-effect that is not fixed?
		this.effect = effect;
		this.size = size;
		this.duration = Optional.ofNullable(duration);
	}

	/**
	 * @return Effect method
	 */
	public EffectMethod getMethod() {
		return effect;
	}

	/**
	 * @return Effect magnitude
	 */
	public Value getSize() {
		return size;
	}

	/**
	 * @return Duration for a transient effect
	 */
	public Optional<Value> getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
