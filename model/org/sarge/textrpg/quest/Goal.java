package org.sarge.textrpg.quest;

import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Player;

/**
 * Quest goal.
 * @author Sarge
 */
public interface Goal {
	/**
	 * Active goal descriptor.
	 */
	public interface ActiveGoal {
		/**
		 * Describes this active goal.
		 * @return Description
		 */
		Description describe();
	}

	/**
	 * Listener for goal updates.
	 */
	public interface Listener {
		/**
		 * Notifies an update for a goal
		 * @param goal			Updated goal
		 * @param completed		Whether the goal is completed
		 */
		void update(ActiveGoal goal, boolean completed);
	}
	
	/**
	 * Starts this goal.
	 * @param player		Player
	 * @param listener		Listener for goal completion
	 * @return Active goal
	 */
	ActiveGoal start(Player player, Listener listener);
}
