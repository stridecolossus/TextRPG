package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.time.LocalDateTime;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Clock;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.TimeCycle;

/**
 * Displays the current world time and date.
 * @author Sarge
 * @see Clock
 * @see TimeCycle
 */
public class TimeAction extends AbstractAction {
	private final Clock clock;
	private final TimeCycle cycle;
	
	/**
	 * Constructor.
	 * @param clock Game-clock
	 * @param cycle Time-cycle
	 */
	public TimeAction(Clock clock, TimeCycle cycle) {
		this.clock = notNull(clock);
		this.cycle = notNull(cycle);
	}

	/**
	 * Displays current world date-time.
	 * @param ctx
	 * @param actor
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse time(Entity actor) throws ActionException {
		// Check outdoors
		// TODO - weather? e.g. cannot tell time in bad storm
		switch(actor.getLocation().getTerrain()) {
		case INDOORS:
		case UNDERGROUND:
			throw new ActionException("info.time.invalid");
		}
		
		// Add world date-time
		// TODO
		// - map to racial calendar and wrap
		// - datetime.txt
		// - this will only work for 30-day months (360 days-per-year), e.g. imladris calendar?
		// - Chronology, LocaleServiceProvider
		final LocalDateTime datetime = LocalDateTime.now();		// TODO - needs to be relative to 'start date-time' of the game and the Clock
		final Description.Builder builder = new Description.Builder("info.time")
			.add("day", datetime.getDayOfMonth())
			.add("month", datetime.getMonthValue())
			.add("year", datetime.getYear());
		
		// Add hour-of-day
		final TimeCycle.Period period = cycle.getPeriod();
		builder
			.add("hour", datetime.getHour())
			.wrap("period", period.getName());
		
		// Build response
		return new ActionResponse(builder.build());
	}
}
