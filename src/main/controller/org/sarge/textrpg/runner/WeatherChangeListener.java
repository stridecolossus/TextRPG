package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.WeatherController;

/**
 * Listener for weather change events that are broadcasts to all players.
 * @author Sarge
 */
public class WeatherChangeListener implements WeatherController.Listener {
	private final SessionManager manager;

	/**
	 * Constructor.
	 * @param manager
	 */
	public WeatherChangeListener(SessionManager manager) {
		this.manager = notNull(manager);
	}

	@Override
	public void update() {
		manager.players().forEach(this::update);
	}

	/**
	 * Updates the given entity with the changed weather.
	 * @param entity Entity
	 */
	private void update(Entity entity) {
		final Description diff = entity.location().area().weather().difference();
		entity.alert(diff);
	}
}
