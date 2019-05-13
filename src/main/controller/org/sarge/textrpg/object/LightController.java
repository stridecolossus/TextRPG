package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.List;

import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Light.Type;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.EmissionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Controller for lights.
 * @author Sarge
 * @see Light
 */
@Controller
public class LightController {
	private static final WorldObject.Filter TINDERBOX = WorldObject.Filter.of(Light.TINDERBOX);
	private static final List<Emission> EMISSIONS = List.of(Emission.LIGHT, Emission.SMOKE);

	private final Event.Queue queue;
	private final ObjectController controller;
	private final EmissionController broadcaster;

	private float threshold = 0.8f;

	/**
	 * Constructor.
	 * @param manager 			Queue manager
	 * @param controller		Object controller
	 * @param broadcaster		Emissions broadcaster
	 */
	public LightController(Event.Queue.Manager manager, ObjectController controller, EmissionController broadcaster) {
		this.queue = manager.queue("queue.lights");
		this.controller = notNull(controller);
		this.broadcaster = notNull(broadcaster);
	}

	/**
	 * Sets the threshold for warning events generated by <b>all</b> lights.
	 * @param threshold Threshold as a percentage of a lights lifetime
	 */
	@Autowired
	public void setWarningThreshold(@Value("${light.warning.threshold}") Percentile threshold) {
		this.threshold = threshold.floatValue();
	}

	/**
	 * Activates the given light.
	 * @param actor		Actor
	 * @param light		Light
	 * @throws ActionException if the light cannot be activated
	 */
	public void light(Entity actor, Light light) throws ActionException {
		// Check for available lighter
		if(!isLighterAvailable(actor)) throw ActionException.of("light.requires.source");

		// Light
		light.light(queue.manager().now());

		// Register warning/expiry events
		register(light);

		// Notify light-level change
		notify(light);

		// Broadcast light notifications
		final var emissions = EMISSIONS.stream()
			.map(e -> new EmissionNotification(e, light.emission(e)))
			.filter(n -> Percentile.ZERO.isLessThan(n.intensity()))
			.collect(toSet());
		broadcaster.broadcast(actor, emissions);
	}

	/**
	 * Snuffs the given light.
	 * @param light Light to snuff
	 * @throws ActionException if the light cannot be snuffed
	 */
	public void snuff(Light light) throws ActionException {
		light.snuff(queue.manager().now());
		notify(light);
		if((light.type() != Type.LANTERN) && !light.isCarried()) {
			controller.decay(light);
		}
	}

	/**
	 * Checks for an available lighter.
	 */
	private static boolean isLighterAvailable(Entity actor) throws ActionException {
		if(actor.contents().find(TINDERBOX).isPresent()) return true;
		if(Light.find(actor.location(), Light.Type.CAMPFIRE).isPresent()) return true;
		return false;
	}

	/**
	 * Registers expiry/warning events for the given light.
	 */
	public void register(Light light) {
		// Register expiry event
		final Event expiry = () -> {
			light.expire();
			raise(light, "light.expired");
			if(light.type() != Light.Type.LANTERN) {
				controller.decay(light);
			}
			return false;
		};
		final long lifetime = light.lifetime();
		register(expiry, lifetime, light.expiry());

		// Register warning event
		final long when = (long) (lifetime * threshold);
		final Event warning = () -> {
			raise(light, "light.warning");
			return false;
		};
		register(warning, when, light.warning());
	}

	/**
	 * Raises a light expiry/warning notification.
	 */
	private static void raise(Light light, String message) {
		final Description description = new Description(message, light.name());
		light.raise(ContentStateChange.of(ContentStateChange.Type.OTHER, description));
	}

	/**
	 * Registers an expiry/warning event.
	 * @param event			Event
	 * @param duration		Duration
	 * @param holder		Event holder
	 */
	private void register(Event event, long duration, Event.Holder holder) {
		final Event.Reference ref = queue.add(event, Duration.ofMillis(duration));
		holder.set(ref);
	}

	/**
	 * Notifies a light-level change.
	 * @param light Light
	 */
	public static void notify(Light light) {
		light.raise(ContentStateChange.LIGHT_MODIFIED);
	}
}