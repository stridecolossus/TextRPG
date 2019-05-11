package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.function.Function;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.EffortAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.HiddenObject;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to hide in the current location.
 * @author Sarge
 */
@EffortAction
@Component
public class HideObjectAction extends AbstractAction {
	private final Duration duration;
	// TODO - move function to HideController
	private final Function<Terrain, Percentile> terrain = t -> Percentile.ONE;
	private final Function<Effort, Percentile> vis = e -> Percentile.ONE;

	/**
	 * Constructor.
	 * @param base Base duration
	 */
	public HideObjectAction(@Value("${hide.object.duration}") Duration base) {
		super(Flag.INDUCTION);
		this.duration = notNull(base);
	}

	/**
	 * Hides an object in the current location.
	 * @param actor		Actor
	 * @param obj		Object to hide
	 * @param effort	Effort
	 * @return Response
	 */
	public Response hide(Entity actor, @Carried(auto=true) WorldObject obj, Effort effort) {
		// Calculate duration
		final Percentile scale = vis.apply(effort);
		final Location loc = actor.location();
		final long ms = (long) (duration.toMillis() * scale.floatValue());

		// Create hide induction
		final Induction induction = () -> {
			// Calculate visibility
			final Percentile level = terrain.apply(loc.terrain()).scale(scale);

			// Hide object
			final HiddenObject hidden = HiddenObject.hide(obj, level, actor);
			hidden.parent(actor.location());
			return Response.OK;
		};

		// Build response
		return Response.of(new Induction.Instance(induction, Duration.ofMillis(ms)));
	}
}
