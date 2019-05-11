package org.sarge.textrpg.util;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;

/**
 * Controller for an activity with intermittent results, e.g. search hidden, recover arrows, etc.
 * @author Sarge
 */
public class DiscoverController<T> {
	private final String name;
	private final Function<T, Percentile> mapper;
	private final Event.Queue queue;

	/**
	 * Constructor.
	 * @param name			Induction identifier
	 * @param mapper		Extracts score from a candidate result
	 * @param queue			Queue for discover events
	 */
	public DiscoverController(String name, Function<T, Percentile> mapper, Event.Queue queue) {
		this.name = notEmpty(name);
		this.mapper = notNull(mapper);
		this.queue = notNull(queue);
	}

	/**
	 * Creates a discover induction.
	 * @param duration		Overall duration
	 * @param stream		Objects to discover
	 * @param consumer		Results consumer
	 * @return Induction
	 */
	public Induction induction(Duration duration, Stream<? extends T> stream, Consumer<T> consumer) {
		Check.notNull(consumer);

		/**
		 * Discovery event.
		 */
		class DiscoverEvent implements Event {
			private final T result;
			private final Percentile diff;

			/**
			 * Constructor.
			 * @param result Discovered event
			 */
			private DiscoverEvent(T result) {
				this.result = result;
				this.diff = mapper.apply(result);
			}

			/**
			 * Schedules this event.
			 */
			private void schedule() {
				final float when = duration.toMillis() * diff.invert().floatValue();
				final long duration = Math.max(1, (long) when);
				queue.add(this, Duration.ofMillis(duration));
			}

			@Override
			public boolean execute() {
				consumer.accept(result);
				return false;
			}
		}

		// Build discover events
		final var results = stream.map(DiscoverEvent::new).filter(e -> !e.diff.isZero()).collect(toList());

		// Schedule events
		results.forEach(DiscoverEvent::schedule);

		// Create induction
		return new Induction() {
			@Override
			public Response complete() throws ActionException {
				// Cleanup
				queue.remove();

				// Build response
				if(results.isEmpty()) {
					return Response.of(TextHelper.join(name, "none.found"));
				}
				else {
					final String key = TextHelper.join(name, "success");
					return Response.of(new Description.Builder(key).add("count", results.size()).build());
				}
			}

			@Override
			public void interrupt() {
				queue.remove();
			}
		};
	}
}
