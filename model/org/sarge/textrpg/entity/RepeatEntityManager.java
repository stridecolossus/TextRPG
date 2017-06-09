package org.sarge.textrpg.entity;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.EventQueue;

/**
 * Entity manager that periodically performs an {@link EntityManager.Action}.
 * @author Sarge
 */
public class RepeatEntityManager implements EntityManager {
	private final EntityManager.Action action;
	private final long period;
	
	/**
	 * Constructor.
	 * @param action Action
	 * @param period Period (ms)
	 */
	public RepeatEntityManager(EntityManager.Action action, long period) {
		Check.notNull(action);
		Check.oneOrMore(period);
		this.action = action;
		this.period = period;
	}

	@Override
	public void start(Entity entity) {
		final EventQueue.Entry entry = entity.getEventQueue().add(() -> action.execute(entity), period, true);
		entity.getEventHolder().set(entry);
		// TODO
	}
	
	@Override
	public void stop(Entity entity) {
		entity.getEventHolder().cancel();
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
