package org.sarge.textrpg.quest;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Quest descriptor.
 * @author Sarge
 */
public final class Quest extends AbstractEqualsObject {
	/**
	 * Quest goal definition.
	 */
	public interface Goal {
		/**
		 * @return New tracker for this goal
		 */
		//Tracker tracker();
	}

	/**
	 *
	 */
	public static final class Stage extends AbstractEqualsObject {
		/**
		 * Policy that specifies the order in which the goals for this stage can be attempted.
		 */
		public enum Policy {
			/**
			 * All goals can be attempted in any order.
			 */
			ANY,

			/**
			 * Goals are attempted in order.
			 */
			SEQUENTIAL
		}

		private final String name;
		private final List<Goal> goals;
		private final Policy policy;

		/**
		 * Constructor.
		 * @param name			Stage name
		 * @param goals			Goal(s)
		 * @param policy		Policy
		 */
		public Stage(String name, List<Goal> goals, Policy policy) {
			this.name = notEmpty(name);
			this.goals = List.copyOf(goals);
			this.policy = notNull(policy);
		}

		/**
		 * @return Stage name
		 */
		public String name() {
			return name;
		}

		/**
		 * @return Goal(s)
		 */
		public List<Goal> goals() {
			return goals;
		}

		/**
		 * @return Policy
		 */
		public Policy policy() {
			return policy;
		}
	}

	private final String name;
	private final List<Stage> stages;
	// TODO - private final Area area;

	/**
	 * Constructor.
	 * @param name			Name of this quest
	 * @param stages		Quest stages
	 */
	public Quest(String name, List<Stage> stages) {
		this.name = notEmpty(name);
		this.stages = List.copyOf(stages);
	}

	/**
	 * @return Name of this quest
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Quest stages
	 */
	public List<Stage> stages() {
		return stages;
	}
}
