package org.sarge.textrpg.quest;

import java.util.concurrent.atomic.AtomicInteger;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.entity.Race;
import org.sarge.textrpg.object.ObjectDescriptor;

/**
 * Collect or massacre goal.
 * @author Sarge
 */
public class CollectGoal implements Goal {
	private final Object target;
	private final int num;
	private final String name;

	/**
	 * Constructor for a collect goal.
	 * @param descriptor	Object to collect
	 * @param num			Number to collect
	 */
	public CollectGoal(ObjectDescriptor descriptor, int num) {
		this(descriptor, num, "goal.collect");
	}

	/**
	 * Constructor for a massacre goal.
	 * @param race			Race
	 * @param num			Number to massacre
	 */
	public CollectGoal(Race race, int num) {
		this(race, num, "goal.massacre");
	}

	/**
	 * Constructor.
	 * @param target		Target
	 * @param num			Number to collect/massacre
	 * @param name			Description key
	 */
	protected CollectGoal(Object target, int num, String name) {
		Check.notNull(target);
		Check.oneOrMore(num);
		Check.notEmpty(name);
		this.target = target;
		this.num = num;
		this.name = name;
	}

	@Override
	public ActiveGoal start(Player player, Listener listener) {
		// Create active goal
		final AtomicInteger count = new AtomicInteger();
		final ActiveGoal active = () -> new Description.Builder(name)
			.add("count", count.get())
			.add("total", num)
			.build();

		// Register listener
		final Player.Listener list = loc -> {
			final boolean finished = count.incrementAndGet() == num;
			listener.update(active, finished);
			return finished;
		};
		player.add(target, list);

		return active;
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
