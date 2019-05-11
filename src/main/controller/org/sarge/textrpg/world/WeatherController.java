package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;
import java.util.Set;

import org.sarge.lib.collection.StrictSet;
import org.sarge.textrpg.util.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Controller for weather updates.
 * @author Sarge
 */
@Component
public class WeatherController {
	/**
	 * Listener for weather updates.
	 */
	public interface Listener {
		/**
		 * Notifies that weather has been updated.
		 */
		void update();
	}

	@Autowired private final Set<Listener> listeners = new StrictSet<>();

	private final Set<Weather> weather = new StrictSet<>();
	private final int max;

	/**
	 * Constructor.
	 * @param max 			Maximum length of weather history
	 * @param period		Iteration period
	 * @param queue			Queue
	 */
	public WeatherController(@Value("${weather.history.size}")int max, @Value("${weather.iteration.period}") Duration period, Event.Queue queue) {
		this.max = oneOrMore(max);
		queue.add(this::randomise, period);
	}

	/**
	 * Registers a new weather model.
	 * @param w Weather
	 */
	public void add(Weather w) {
		weather.add(w);
	}

	/**
	 * Register a listener for weather updates.
	 * @param listener Listener
	 */
	public void add(Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Randomises all weather models.
	 */
	boolean randomise() {
		// Update weather
		for(Weather w : weather) {
			w.randomise(max);
		}

		// Notify listeners
		listeners.forEach(Listener::update);

		return true;
	}
}
