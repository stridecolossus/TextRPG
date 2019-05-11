package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.Percentile;

/**
 * Descriptor for a skill that can be used to perform actions.
 * <p>
 * A series of related skills is known as a <i>group</i> and is used to model a tiered list of skills, e.g. basic, intermediate, advanced, etc.
 * {@link Builder#previous(Skill)} is used to add a skill to the end of the group.
 * <p>
 * In addition to groups, skills can also have any other skills as dependencies specified using {@link Builder#required(Skill)}.
 * <p>
 * By default all skills are mandatory, i.e. an entity must possess the skill in order to perform some action.
 * However skills may be defined as optional by providing a default skill-score which is used if the entity does not possess the skill,
 * see {@link Builder#defaultScore(Percentile)} and {@link #defaultSkill()}.
 * <p>
 * Notes:
 * <ul>
 * <li>A skill can only have one previous skill (and therefore is a member of a single group)</li>
 * <li>Only stand-alone skills or the first skill in a group can be declared as optional</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public final class Skill extends AbstractEqualsObject implements CommandArgument {
	/**
	 * Empty skill.
	 */
	public static final Skill NONE = new Skill.Builder().name("none").build();

	private final String name;
	private final int power;
	private final Percentile score;
	private final Calculation mod;
	private final Duration duration;
	private final int scale;
	private final int xp;
	private final List<Skill> required;
	private final Skill group;
	private final Skill def;
	private Optional<Skill> next = Optional.empty();

	/**
	 * Constructor.
	 * @param name			Name
	 * @param power			Power cost
	 * @param score			Score
	 * @param def			Default score if this is an optional skill or <tt>null</tt> if mandatory
	 * @param mod			Score modifier
	 * @param duration		Base duration
	 * @param scale			Skill effect multiplier
	 * @param xp			XP points cost
	 * @param required		Dependent skills
	 * @param prev			Previous skill in this group (optional)
	 * @throws IllegalArgumentException if the given previous skill is already a member of a skill-group
	 * @throws IllegalArgumentException if the requirements contains the previous skill
	 */
	private Skill(String name, int power, Percentile score, Percentile def, Calculation mod, Duration duration, int scale, int xp, Collection<Skill> required, Skill prev) {
		this.name = notEmpty(name);
		this.power = zeroOrMore(power);
		this.score = notNull(score);
		this.mod = notNull(mod);
		this.duration = notNull(duration);
		this.scale = zeroOrMore(scale);
		this.xp = zeroOrMore(xp);

		// Link skill group
		if(prev == null) {
			this.group = this;
			this.required = List.copyOf(required);

			// Init proxy for default skill
			if(def == null) {
				this.def = null;
			}
			else {
				this.def = new Skill(name, power, def, mod, duration, scale, this);
			}
		}
		else {
			// Check is mandatory
			if(def != null) throw new IllegalStateException("Only first skill in the group can be optional");
			this.def = null;

			// Validate previous skill
			if(prev.next.isPresent()) throw new IllegalArgumentException(String.format("Invalid previous skill: prev=%s this=%s", prev.name, name));
			if(required.contains(prev)) throw new IllegalArgumentException(String.format("Previous skill cannot be a requirement: prev=%s this=%s", prev.name, name));
			this.group = prev.group;
			prev.next = Optional.of(this);

			// Build required skills
			final List<Skill> req = new ArrayList<>(required);
			req.add(prev);
			this.required = List.copyOf(req);
		}
	}

	private Skill(String name, int power, Percentile score, Calculation mod, Duration duration, int scale, Skill group) {
		this.name = notEmpty(name);
		this.power = zeroOrMore(power);
		this.score = notNull(score);
		this.mod = notNull(mod);
		this.duration = notNull(duration);
		this.scale = zeroOrMore(scale);
		this.xp = 0;
		this.required = List.of();
		this.group = notNull(group);
		this.def = null;
	}

	/**
	 * @return Whether this is a mandatory skill
	 */
	public boolean isMandatory() {
		return def == null;
	}

	/**
	 * @return Name of this skill
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @return Power cost to use this skill
	 */
	public int power() {
		return power;
	}

	/**
	 * @return Skill score
	 */
	public Percentile score() {
		return score;
	}

	/**
	 * @return Default skill
	 */
	public Skill defaultSkill() {
		return def;
	}

	/**
	 * @return Skill-score modifier
	 */
	public Calculation modifier() {
		return mod;
	}

	/**
	 * @return Base duration
	 */
	public Duration duration() {
		return duration;
	}

	/**
	 * @return Skill effect multiplier
	 */
	public int scale() {
		return scale;
	}

	/**
	 * @return XP point cost of this skill
	 */
	public int cost() {
		return xp;
	}

	/**
	 * @return Dependent skills
	 */
	public List<Skill> required() {
		return required;
	}

	/**
	 * @return Next skill in this group
	 */
	public Optional<Skill> next() {
		return next;
	}

	/**
	 * @return Initial skill in this group
	 */
	public Skill group() {
		return group;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Builder for a skill descriptor.
	 */
	public static class Builder {
		private String name;
		private int power;
		private Percentile score = Percentile.ONE;
		private Percentile def;
		private Calculation mod = Calculation.ZERO;
		private Duration duration = Duration.ZERO;
		private int scale = 1;
		private int cost = 1;
		private final List<Skill> required = new StrictList<>();
		private Skill prev;

		/**
		 * Sets the name of this skill.
		 * @param name Skill name
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the default score for this optional skill.
		 * @param def Default score
		 */
		public Builder defaultScore(Percentile def) {
			this.def = def;
			return this;
		}

		/**
		 * Sets the power cost of this skill.
		 * @param power Power cost
		 */
		public Builder power(int power) {
			this.power = power;
			return this;
		}

		/**
		 * Sets the score of this skill.
		 * @param score Score
		 */
		public Builder score(Percentile score) {
			this.score = score;
			return this;
		}

		/**
		 * Sets the score modifier for this skill.
		 * @param mod Modifier
		 */
		public Builder modifier(Calculation mod) {
			this.mod = mod;
			return this;
		}

		/**
		 * Sets the duration modifier of this skill.
		 * @param duration Base duration
		 */
		public Builder duration(Duration duration) {
			this.duration = duration;
			return this;
		}

		/**
		 * Sets the effect multiplier of this skill.
		 * @param scale Multiplier
		 */
		public Builder scale(int scale) {
			this.scale = scale;
			return this;
		}

		/**
		 * Sets the XP point cost of this skill.
		 * @param cost XP point cost
		 */
		public Builder cost(int cost) {
			this.cost = cost;
			return this;
		}

		/**
		 * Adds a required skill.
		 * @param skill Dependent skill
		 */
		public Builder required(Skill skill) {
			required.add(skill);
			return this;
		}

		/**
		 * Sets the previous skill of this group.
		 * @param prev Previous skill
		 */
		public Builder previous(Skill prev) {
			this.prev = prev;
			return this;
		}

		/**
		 * Constructs this skill.
		 * @return New skill descriptor
		 */
		public Skill build() {
			return new Skill(name, power, score, def, mod, duration, scale, cost, required, prev);
		}
	}
}
