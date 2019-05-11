package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.LocalDate;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.NumericConverter;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.Clock;
import org.sarge.textrpg.util.Clock.DateTimeClock;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.PeriodModel;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.stereotype.Component;

/**
 * Action to display the current date and time.
 * @author Sarge
 */
@Component
public class TimeAction extends AbstractAction {
	private final DateTimeClock clock;
	private final PeriodModel<TimePeriod> model;
	private final NumericConverter converter;

	/**
	 * Constructor.
	 * @param clock		Clock
	 * @param time 		Time model
	 */
	public TimeAction(Clock clock, PeriodModel<TimePeriod> model, NumericConverter converter) {
		super(Flag.OUTSIDE);
		this.clock = DateTimeClock.of(clock);
		this.model = notNull(model);
		this.converter = notNull(converter);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	protected boolean isValid(Stance stance) {
		return true;
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Displays the current date and time.
	 * @return Response
	 */
	@RequiresActor
	public Response time(Entity actor) {
		// Determine period
		final String period = TextHelper.join("period", model.current().name());

		// Determine current date
		final LocalDate now = clock.toDateTime().toLocalDate();
		final Calendar calendar = actor.descriptor().faction().get().calendar();
		final Calendar.Date date = calendar.date(now);

		// Create time description
		final String name = calendar.name();
		final String key = TextHelper.join("action.time", date.isSpecial() ? "special": "default");
		final Description.Builder response = new Description.Builder(key)
			.add("period", period)
			.add("month", TextHelper.join(name, date.month()))
			.add("year", date.year())
			.add("era", TextHelper.join(calendar.name(), "era"));

		// Add day/weekday
		if(!date.isSpecial()) {
			response.add("day", format(date.day()), ArgumentFormatter.PLAIN);
			response.add("weekday", TextHelper.join(name, "weekday", date.weekday()));
		}

		// Build response
		return Response.of(response.build());
	}

	/**
	 * Formats the month day.
	 */
	private String format(int day) {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(day));
		sb.append(converter.suffix(day));
		return sb.toString();
	}
}
