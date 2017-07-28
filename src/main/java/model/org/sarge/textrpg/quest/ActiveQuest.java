package org.sarge.textrpg.quest;

import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.quest.Goal.ActiveGoal;
import org.sarge.textrpg.quest.Quest.Stage;

/**
 * Active quest record.
 * @author Sarge
 */
public class ActiveQuest {
	/**
	 * Call-back listener for quest completion.
	 */
	public interface Listener {
		/**
		 * Notifies this quest has been completed.
		 */
		void completed();
	}
	
	private final Goal.Listener listener;
	private final Listener callback;
	private final Iterator<Quest.Stage> itr;

	private Stage stage;
	private List<ActiveGoal> goals;

	/**
	 * Constructor.
	 * @param quest			Quest
	 * @param player		Player
	 * @param callback		Call-back listener for quest completion
	 */
	public ActiveQuest(Quest quest, Player player, Listener callback) {
		Check.notNull(player);
		this.listener = (goal, done) -> update(goal, done, player);
		this.callback = callback;
		this.itr = quest.iterator();
		assert itr.hasNext();
		next(player);
	}
	
	/**
	 * @return Current stage
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * @return Active goals
	 */
	public Stream<ActiveGoal> getActiveGoals() {
		return goals.stream();
	}

	/**
	 * Starts the next stage in this quest.
	 * @param player Player
	 */
	private void next(Player player) {
		assert goals == null;
		stage = itr.next();
		goals = stage.getGoals().map(goal -> goal.start(player, listener)).collect(toList());
	}
	
	/**
	 * Updates a goal.
	 * @param goal			Goal
	 * @param done			Whether this goal has been completed
	 * @param player		Player
	 */
	protected void update(ActiveGoal goal, boolean done, Player player) {
		// Notify update
		player.alert(goal.describe().toNotification());
		
		// Remove completed goals
		if(done) {
			assert goals.contains(goal);
			goals.remove(goal);
		}
		
		// Update quest
		if(goals.isEmpty()) {
			goals = null;
			if(itr.hasNext()) {
				// Start next stage
				next(player);
			}
			else {
				// Notify quest completed
				stage = null;
				callback.completed();
			}
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
