package org.sarge.textrpg.quest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Condition;

/**
 * Quest descriptor.
 * @author Sarge
 */
public final class Quest {
	/**
	 * Stage in this quest.
	 */
	public static final class Stage {
		private final String name;
		private final List<Goal> goals;
		private final Reward reward;
		
		/**
		 * Constructor.
		 * @param name			Stage name
		 * @param goal			Goal(s)
		 * @param reward		Reward(s)
		 */
		public Stage(String name, List<Goal> goals, Reward reward) {
			Check.notEmpty(name);
			Check.notNull(goals);
			Check.notNull(reward);
			this.name = name;
			this.goals = new ArrayList<>(goals);
			this.reward = reward;
		}
		
		public String getName() {
			return name;
		}
		
		public Stream<Goal> getGoals() {
			return goals.stream();
		}
		
		public Reward getReward() {
			return reward;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private final String name;
	private final Condition condition;
	private final List<Stage> stages;
	
	/**
	 * Constructor.
	 * @param name				Quest name
	 * @param condition			Condition(s) to start this quest
	 * @param stages			Quest stages
	 */
	public Quest(String name, Condition condition, List<Stage> stages) {
		Check.notEmpty(name);
		Check.notNull(condition);
		Check.notEmpty(stages);
		this.name = name;
		this.condition = condition;
		this.stages = stages;
	}
	
	public String getName() {
		return name;
	}
	
	public Condition getCondition() {
		return condition;
	}
	
	public Iterator<Stage> iterator() {
		return stages.iterator();
	}

	@Override
	public String toString() {
		return name;
	}
}
