package org.sarge.textrpg.entity;

import java.util.ArrayList;
import java.util.List;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.util.Percentile;

/**
 * Skill descriptor.
 * @author Sarge
 */
public class Skill {
	/**
	 * Skill tier.
	 */
	public static final class Tier {
		private final Condition condition;
		private final int cost;

		/**
		 * Constructor.
		 * @param condition		Condition(s) to earn this tier
		 * @param cost			XP cost
		 */
		public Tier(Condition condition, int cost) {
			Check.notNull(condition);
			Check.oneOrMore(cost);
			this.condition = condition;
			this.cost = cost;
		}
		
		public Condition getCondition() {
			return condition;
		}
		
		public int getCost() {
			return cost;
		}
		
		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}
	
	private final String name;
	private final List<Tier> tiers;
	
	/**
	 * Constructor.
	 * @param name		Skill identifier
	 * @param tiers		Skill tiers
	 */
	public Skill(String name, List<Tier> tiers) {
		Check.notEmpty(name);
		Check.notEmpty(tiers);
		this.name = name;
		this.tiers = new ArrayList<>(tiers);
	}
	
	/**
	 * @return Skill identifier
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return Maximum tier
	 */
	public int getMaximum() {
		return tiers.size();
	}

	/**
	 * Looks up a skill tier.
	 * @param level Skill level
	 * @return Tier
	 */
	public Tier getTier(int level) {
		return tiers.get(level);
	}

	/**
	 * Helper - Creates a percentile representing the given level of this skill.
	 * @param level Skill level
	 * @return Skill percentile
	 */
	public Percentile toPercentile(int level) {
		return new Percentile(level / (float) tiers.size());
	}
	
	/**
	 * Helper - Creates a condition requiring this skill at the given level.
	 * @param level Skill level
	 * @return Skill condition
	 */
	public Condition condition(int level) {
		// TODO
		return actor -> true;
	}

	@Override
	public String toString() {
		return name;
	}
}
