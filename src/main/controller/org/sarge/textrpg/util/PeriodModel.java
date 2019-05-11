package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.util.PeriodModel.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Model for a periodic set of events.
 * @param <T> Period type
 * @author Sarge
 */
public interface PeriodModel<T extends Period> {
	/**
	 * @return Current period
	 */
	T current();

	/**
	 * Registers a listener on this instance.
	 * @param listener Listener
	 */
	void add(Listener<T> listener);

	/**
	 * Period.
	 */
	interface Period {
		/**
		 * @return Start-time of this period
		 */
		LocalTime start();
	}

	/**
	 * Listener for a new period.
	 * @param <T> Period type
	 */
	interface Listener<T extends Period> {
		/**
		 * Notifies the start of a new period.
		 * @param period Period
		 */
		void update(T period);
	}

	/**
	 * Factory for period models.
	 */
	@Component
	class Factory extends AbstractObject {
		private static final Logger LOG = LoggerFactory.getLogger(PeriodModel.class);
		private static final Duration DAY = Duration.ofDays(1);

		private final Event.Queue queue;
		private final Clock.DateTimeClock clock;

		/**
		 * Constructor.
		 * @param queue Event queue for period events
		 */
		public Factory(Event.Queue queue) {
			this.queue = notNull(queue);
			this.clock = Clock.DateTimeClock.of(queue.manager());
		}

		/**
		 * Creates a new period model.
		 * @param periods Periods
		 * @return New period model
		 */
		public <T extends Period> PeriodModel<T> create(List<T> periods) {
			return new DefaultPeriodModel<>(periods);
		}

		/**
		 * Calculates the relative duration between the given times (wrapping around a day).
		 */
		private static Duration duration(LocalTime prev, LocalTime next) {
			final Duration duration = Duration.between(prev, next);
			if(duration.isNegative()) {
				return DAY.plus(duration);
			}
			else {
				return duration;
			}
		}

		/**
		 * Default implementation.
		 * @param <T> Period type
		 */
		private class DefaultPeriodModel<T extends Period> extends AbstractEqualsObject implements PeriodModel<T> {
			private final Set<Listener<T>> listeners = new StrictSet<>();
			private final List<T> periods;
			private int current;

			/**
			 * Constructor.
			 * @param periods Periods
			 */
			private DefaultPeriodModel(List<T> periods) {
				this.periods = List.copyOf(periods);
				verify(periods);
				start();
			}

			/**
			 * Checks periods are ascending.
			 */
			private void verify(List<T> periods) {
				if(periods.size() < 2) throw new IllegalArgumentException("Periods must have at least two entries");
				LocalTime prev = LocalTime.MIN;
				for(T next : periods) {
					if(prev.isAfter(next.start())) {
						throw new IllegalArgumentException("Periods must be ascending");
					}
					prev = next.start();
				}
			}

			/**
			 * Starts this instance.
			 */
			private void start() {
				// Determine current period
				final LocalTime now = clock.toDateTime().toLocalTime();
				final int count = (int) periods.stream().filter(p -> p.start().isBefore(now) || p.start().equals(now)).count();

				// Determine previous period and clamp current period
				final int first;
				if(count == 0) {
					// Before first period
					current = periods.size() - 1;
					first = 0;
				}
				else
				if(count == periods.size()) {
					// After last period
					current = 0;
					first = periods.size() - 1;
				}
				else {
					// Between periods
					current = count - 1;
					first = count;
				}

				// Register next period
				final Duration duration = duration(now, periods.get(first).start());
				update(current);
				register(duration);
			}

			/**
			 * Register a transition event for the next period.
			 */
			private void register(Duration duration) {
				assert !duration.isZero() && !duration.isNegative();
				final Event event = () -> {
					// Move to next period
					final LocalTime prev = current().start();
					++current;
					if(current == periods.size()) {
						current = 0;
					}

					// Notify period transition
					update(current);

					// Register next period
					final Duration next = duration(prev, periods.get(current).start());
					register(next);

					return false;
				};
				queue.add(event, duration);
			}

			@Override
			public T current() {
				return periods.get(current);
			}

			@Override
			public void add(Listener<T> listener) {
				listeners.add(listener);
			}

			/**
			 * Notifies listeners of a period transition.
			 * @param period Next period
			 */
			private void update(int index) {
				final T period = periods.get(index);
				LOG.info("Update period: index={} current={} period={}", index, current, period);
				for(Listener<T> listener : listeners) {
					listener.update(period);
				}
			}
		}
	}
}
