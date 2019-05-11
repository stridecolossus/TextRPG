package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Notification;
import org.sarge.textrpg.entity.PerceptionCalculator;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Controller for emission notifications and detection.
 * @see LocationBroadcaster
 * @author Sarge
 */
@Controller
public class EmissionController {
	private final PerceptionCalculator calc;
	private final Percentile scale;
	private final LocationBroadcaster visitor;

	private Percentile threshold = Percentile.ZERO;

	/**
	 * Constructor.
	 * @param calc			Perception calculator
	 * @param max			Maximum link traversal depth
	 * @param scale			Emission intensity scale (per link traversal)
	 */
	public EmissionController(PerceptionCalculator calc, @Value("${emissions.traversal.depth}") int max, @Value("${emissions.intensity.scale}") Percentile scale) {
		this.calc = notNull(calc);
		this.scale = notNull(scale);
		this.visitor = new LocationBroadcaster(max);
	}

	/**
	 * Sets the minimum threshold for notification perception.
	 * @param threshold Minimum threshold
	 */
	@Autowired
	public void setMinimumThreshold(@Value("${emissions.threshold}") Percentile threshold) {
		this.threshold = notNull(threshold);
	}

	/**
	 * Broadcasts notifications to neighbouring locations.
	 * @param actor				Actor
	 * @param notifications 	Notifications
	 */
	public void broadcast(Entity actor, Set<EmissionNotification> notifications) {
		// Broadcast to current location
		for(EmissionNotification notification : notifications) {
			broadcast(actor, actor.location(), notification, notification.intensity());
		}

		// Broadcast to neighbouring locations
		final LocationBroadcaster.Visitor broadcaster = (exit, depth) -> {
			final Location loc = exit.destination();
			for(EmissionNotification notification : notifications) {
				final EmissionNotification scaled = notification.scale(scale, depth, exit.direction());
				broadcast(null, loc, scaled, scaled.intensity());
			}
		};
		visitor.visit(actor.location(), broadcaster);
	}

	/**
	 * Broadcasts a notification to entities in the given location.
	 * @param actor					Optional actor
	 * @param loc					Location
	 * @param notification			Notification
	 * @param diff					Detection difficulty
	 */
	public <T extends Notification> void broadcast(Entity actor, Location loc, T notification, Percentile diff) {
		// Ignore if too difficult to detect
		if(diff.isLessThan(threshold)) {
			return;
		}

		// Otherwise broadcast to entities that can perceive the given notification
		loc.contents().select(Entity.class)
			.filter(e -> e != actor)
			.filter(e -> diff.isLessThan(calc.score(e)))
			.forEach(e -> notification.handle(e.manager().handler(), e));
	}

	/**
	 * Finds emissions in neighbouring locations.
	 * @param emissions		Types of emission
	 * @param start			Start location
	 * @return Emissions
	 */
	public Collection<EmissionNotification> find(Set<Emission> emissions, Location start) {
		final List<EmissionNotification> notifications = new ArrayList<>();
		final LocationBroadcaster.Visitor finder = (exit, depth) -> {
			for(Emission emission : emissions) {
				final Percentile intensity = exit.destination().emission(emission);
				final EmissionNotification notification = new EmissionNotification(emission, intensity);
				final EmissionNotification scaled = notification.scale(scale, depth, exit.direction());
				notifications.add(scaled);
			}
		};
		visitor.visit(start, finder);
		return notifications;
	}
}
