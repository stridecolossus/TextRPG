package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.AbstractAction.Effort;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.MovementMode;
import org.sarge.textrpg.object.Rope;
import org.sarge.textrpg.util.Clock;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * Controller for {@link Tracks} generation and detection.
 * @author Sarge
 */
@Controller
public class TracksController {
	private final Map<Area, Percentile> weathering = new ConcurrentHashMap<>();
	private final Clock clock;

	private long lifetime = Duration.ofDays(1).toMillis();
	private Function<Terrain, Percentile> terrain = t -> Percentile.ONE;
	private Function<Route, Percentile> route = r -> Percentile.ONE;
	private Function<Effort, Float> effort = e -> 1f;
	private float scale = 0.5f;

	/**
	 * Constructor.
	 * @param clock Game clock
	 */
	public TracksController(Clock clock) {
		this.clock = notNull(clock);
	}

	/**
	 * Sets the lifetime of new tracks.
	 * @param lifetime Tracks lifetime
	 */
	@Autowired
	public void setLifetime(@Value("${tracks.lifetime}") Duration lifetime) {
		DurationConverter.oneOrMore(lifetime);
		this.lifetime = lifetime.toMillis();
	}

	/**
	 * Sets the effort modifier when detecting tracks.
	 * @param effort Effort modifier
	 */
	@Autowired
	public void setEffortModifier(@Value("#{effort.function('track', T(org.sarge.lib.util.Converter).FLOAT)}")Function<Effort, Float> effort) {
		this.effort = notNull(effort);
	}

	/**
	 * Sets the terrain visibility modifier for new tracks.
	 * @param terrain Terrain modifier
	 */
	@Autowired
	public void setTerrainVisibilityModifier(@Value("#{terrain.function('tracks', T(org.sarge.textrpg.util.Percentile).CONVERTER)}")Function<Terrain, Percentile> terrain) {
		this.terrain = notNull(terrain);
	}

	/**
	 * Sets the route-type visibility modifier for new tracks.
	 * @param route Route modifier
	 */
	@Autowired
	public void setRouteVisibilityModifier(@Value("#{route.function('tracks', T(org.sarge.textrpg.util.Percentile).CONVERTER)}")Function<Route, Percentile> route) {
		this.route = notNull(route);
	}

	/**
	 * Sets the scaling factor for historical weather.
	 * @param scale Weather scaling factor
	 * @throws IllegalArgumentException if the given scale is zero
	 */
	@Autowired
	public void setWeatherScale(@Value("${tracks.weather.scale}") float scale) {
		Check.isPercentile(scale);
		if(Float.floatToIntBits(scale) == 0) throw new IllegalArgumentException("Weather scale cannot be zero");
		this.scale = scale;
	}

	/**
	 * Movement listener that generates tracks.
	 */
	@Component
	private class TracksMovementListener implements MovementController.Listener {
		@Override
		public void update(Entity actor, Exit exit, Location prev) {
			if(isTracksLocation(prev, exit)) {
				add(actor, exit, prev);
			}
		}
	}

	/**
	 * Determines whether tracks are generated in the given location.
	 * @param loc		Location
	 * @param exit		Exit
	 * @return Whether to add tracks
	 */
	static boolean isTracksLocation(Location loc, Exit exit) {
		// Ignore unsuitable terrain
		switch(loc.terrain()) {
		case INDOORS:
		case URBAN:
		case WATER:
			return false;
		}

		// Ignore rope links
		if(exit.link() instanceof Rope.RopeLink) {
			return false;
		}

		return true;
	}

	/**
	 * Generates a new set of tracks.
	 * @param actor		Actor
	 * @param exit		Exit
	 * @param prev		Previous location
	 */
	void add(Entity actor, Exit exit, Location prev) {
		// Calculate tracks visibility
		final MovementMode movement = actor.movement();
		final Percentile base = movement.tracks();
		final Percentile vis = visibility(prev, exit).scale(base);

		// Lookup trail for this actor
		final Trail trail = actor.movement().trail();
		trail.prune(lifetime);

		// Add tracks
		if(!vis.isZero()) {
			final String name = movement.mover().name();
			final Tracks tracks = new Tracks(prev, name, exit.direction(), vis, clock.now(), trail.previous());
			trail.add(tracks);
			prev.add(tracks);
		}
	}

	/**
	 * Calculates the <i>initial</i> tracks visibility modifier for the given location.
	 * @param prev 		Previous location
	 * @param exit		Exit
	 * @return Visibility modifier
	 */
	private Percentile visibility(Location prev, Exit exit) {
		final Area area = prev.area();
		final Percentile t = terrain.apply(prev.terrain());
		final Percentile r = route.apply(exit.link().route());
		final Percentile w = weathering.computeIfAbsent(area, this::weathering);
		return t.scale(r).scale(w);
	}

	/**
	 * Listener for weather updates that clears the cached weathering values.
	 */
	@Bean
	private WeatherController.Listener listener() {
		return weathering::clear;
	}

	/**
	 * Calculates the tracks visibility modifier due to weathering.
	 * @param area Area
	 * @return Visibility modifier
	 */
	private Percentile weathering(Area area) {
		final Weather weather = area.weather();
		final float total = (float) weather.history().mapToDouble(TracksController::weathering).reduce(1, (a, b) -> a * b * scale);
		return new Percentile(total);
	}

	/**
	 * Calculates weathering modifier for the given weather entry.
	 * @param entry Weather entry
	 * @return Modifier (as a percentile floating-point value)
	 */
	private static float weathering(Weather.Entry entry) {
		final float p = entry.get(Weather.Component.PRECIPITATION);
		final float t = entry.get(Weather.Component.TEMPERATURE);
		final float total = p + t / 2f;
		return total / 3f;
	}
}
