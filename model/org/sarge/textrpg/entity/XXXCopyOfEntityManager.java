package org.sarge.textrpg.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.Clock;
import org.sarge.textrpg.common.Event;

/**
 * Entity AI manager.
 * @author Sarge
 */
public class XXXCopyOfEntityManager {
	/**
	 * Static entity.
	 */
	public static final XXXCopyOfEntityManager STATIC = new XXXCopyOfEntityManager() {
		@Override
		public void start(Entity entity) {
			// Does nothing
		}
	};

	/**
	 * Entity-state.
	 */
	public interface EntityState {
		/**
		 * Performs this action.
		 * @param entity Entity
		 */
		void execute(Entity entity);
	}
	
	/**
	 * Period descriptor.
	 */
	public static class Period {
		private final EntityState action;
		private final int start, end;
		private final long period;
		
		/**
		 * Constructor.
		 * @param action		Action to perform during this period
		 * @param start			Start hour 0..23
		 * @param end			End hour
		 * @param period		Iteration period (ms)
		 */
		public Period(EntityState action, int start, int end, long period) {
			Check.notNull(action);
			Check.zeroOrMore(start);
			Check.zeroOrMore(end);
			Check.zeroOrMore(period);
			if(end < start) throw new IllegalArgumentException("Cannot end before start!");
			if(period > (end - start) * Clock.HOUR) throw new IllegalArgumentException("Iteration period is too long");
			this.action = action;
			this.start = start;
			this.end = end;
			this.period = period;
		}

		/**
		 * Schedules the next action.
		 * @param entity Entity
		 */
		public void schedule(Entity entity, XXXCopyOfEntityManager manager) {
			entity.getEventQueue().add(() -> next(entity, manager), period);
		}
		
		/**
		 * Performs the next action.
		 * @param entity Entity
		 */
		private void next(Entity entity, XXXCopyOfEntityManager manager) {
			// Perform action
			action.execute(entity);
			
			if(end < Clock.CLOCK.getHour()) {
				// Schedule next action
				schedule(entity, manager);
			}
			else {
				// Otherwise start next period
				manager.start(entity);
			}
		}

		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}
	
	private final List<Period> periods;
	private final EntityState def;
	
	/**
	 * Default constructor for a static entity.
	 */
	private XXXCopyOfEntityManager() {
		periods = null;
		def = null;
	}

	/**
	 * Constructor.
	 * @param def		Default action to perform when no period is active
	 * @param periods	Periods
	 * @throws IllegalArgumentException if the list of periods is empty or any periods overlap
	 */
	public XXXCopyOfEntityManager(EntityState def, List<Period> periods) {
		Check.notNull(def);
		Check.notEmpty(periods);
		this.def = def;
		this.periods = new ArrayList<>(periods);
		verify();
	}

	/**
	 * Checks for overlapping periods.
	 */
	private void verify() {
		final Iterator<Period> itr = periods.iterator();
		Period prev = itr.next();
		while(itr.hasNext()) {
			final Period next = itr.next();
			verify(prev, next);
			verify(next, prev);
			prev = next;
		}
	}

	/**
	 * Checks for an overlapping period.
	 */
	private static void verify(Period a, Period b) {
		if((a.start > b.start) && (a.start < b.end)) throw new IllegalArgumentException(String.format("Overlapping periods: %s %s", a, b));
	}

	/**
	 * Starts or resumes this manager on the given entity.
	 * @param entity Entity
	 */
	public void start(Entity entity) {
		final int hour = Clock.CLOCK.getHour();
		final Period period = periods.stream()
			.filter(p -> p.end > hour)
			.findFirst()
			.orElseGet(() -> periods.get(0));
		
		if((period.start <= hour) && (period.end > hour)) {
			// Start period now
			period.schedule(entity, this);
		}
		else {
			// Determine when to start
			final long when;
			if(hour >= period.end) {
				// Period starts in the next day
				when = (24 - hour) + period.start;
			}
			else {
				// Period starts later in this day
				when = period.start - hour;
			}
			
			// Schedule period
			final Event e = () -> period.schedule(entity, this);
			entity.getEventQueue().add(e, when * Clock.HOUR);
			
			// Perform default action until period starts
			def.execute(entity);
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
