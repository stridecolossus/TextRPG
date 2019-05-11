package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.ValueModifier;

/**
 * Effect descriptor.
 * @author Sarge
 *
 * TODO
 * infections
 * => HP, sta, str, end, etc.
 * => concentration
 *
 */
public final class Effect extends AbstractEqualsObject {
	/**
	 * Effect that does nothing.
	 */
	public static final Effect NONE = new Effect();

	/**
	 * Effect group for categorised removal.
	 */
	public enum Group {
		DEFAULT,
		POISON,
		DISEASE,
		WOUND
	}

	private final String name;
	private final ValueModifier.Key mod;
	private final Calculation size;
	private final Group group;
	private final Duration duration;
	private final int times;

	/**
	 * Constructor.
	 * @param name			Name
	 * @param mod			Effect modifier
	 * @param size			Magnitude
	 * @param group			Effect group
	 * @param duration		Duration or {@link Duration#ZERO} for a fixed effect
	 * @param times			Number of times to apply this effect (one-or-more)
	 * @throws IllegalArgumentException if this effect entry is not valid
	 */
	public Effect(String name, ValueModifier.Key mod, Calculation size, Group group, Duration duration, int times) {
		if((times > 1) && duration.isZero()) throw new IllegalArgumentException("Repeating effects must have a duration");
		this.name = notEmpty(name);
		this.mod = notNull(mod);
		this.size = notNull(size);
		this.group = notNull(group);
		this.duration = notNull(duration);
		this.times = oneOrMore(times);
	}

	/**
	 * Empty effect constructor.
	 */
	private Effect() {
		name = null;
		mod = null;
		size = null;
		group = null;
		duration = null;
		times = 0;
	}

	/**
	 * @return Effect name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Effect modifier method
	 */
	public ValueModifier.Key modifier() {
		return mod;
	}

	/**
	 * @return Effect magnitude
	 */
	public Calculation size() {
		return size;
	}

	/**
	 * @return Effect group
	 */
	public Group group() {
		return group;
	}

	/**
	 * @return Duration
	 */
	public Duration duration() {
		return duration;
	}

	/**
	 * @return Number of times to apply this effect
	 */
	public int times() {
		return times;
	}

	/**
	 * Builder for an effect descriptor.
	 */
	public static class Builder {
		private static final Calculation INVALID = ignore -> {
			throw new UnsupportedOperationException();
		};

		private String name;
		private ValueModifier.Key mod;
		private Calculation size = INVALID;
		private Group group = Group.DEFAULT;
		private Duration duration = Duration.ZERO;
		private int times = 1;

		/**
		 * Sets the name of this effect.
		 * @param name Name
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the effect modifier.
		 * @param mod Modifier
		 */
		public Builder modifier(ValueModifier.Key mod) {
			this.mod = mod;
			return this;
		}

		/**
		 * Sets the magnitude of this effect.
		 * @param size Size
		 */
		public Builder size(Calculation size) {
			this.size = size;
			return this;
		}

		/**
		 * Sets the group of this effect.
		 * @param group Effect group
		 */
		public Builder group(Group group) {
			this.group = group;
			return this;
		}

		/**
		 * Sets the duration of this effect.
		 * @param duration Duration
		 */
		public Builder duration(Duration duration) {
			this.duration = duration;
			return this;
		}

		/**
		 * Sets the number of times this effect is applied.
		 * @param times Number of times to apply
		 */
		public Builder times(int times) {
			this.times = times;
			return this;
		}

		/**
		 * Constructs this effect.
		 * @return New effect
		 */
		public Effect build() {
			return new Effect(name, mod, size, group, duration, times);
		}
	}
}
