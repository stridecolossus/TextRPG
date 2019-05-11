package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.ObjectController;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to butcher a corpse.
 * @author Sarge
 */
@Component
public class ButcherAction extends AbstractAction {
	private final Duration base;
	private final ObjectController controller;

	/**
	 * Constructor.
	 * @param duration Base duration
	 */
	public ButcherAction(@Value("${butcher.duration}") Duration duration, ObjectController controller) {
		super(Flag.REVEALS, Flag.INDUCTION, Flag.BROADCAST);
		this.base = notNull(duration);
		this.controller = notNull(controller);
	}

	/**
	 * Butchers a corpse.
	 * @param actor			Actor
	 * @param corpse		Corpse
	 * @return Response
	 * @throws ActionException if the given corpse cannot be butchered
	 */
	@RequiresActor
	@RequiredObject("hunting.knife")
	public Response butcher(Entity actor, Corpse corpse) throws ActionException {
		// Create butcher induction
		final Induction induction = () -> {
			// Butcher and generate loot
			final var loot = corpse.butcher(actor).collect(toList());

			// Register decay events
			loot.forEach(controller::decay);

			// Move to inventory
			final InventoryController inv = new InventoryController("butcher");
			final var results = inv.take(actor, loot.stream());

			// Build response
			final Response.Builder builder = new Response.Builder();
			builder.add(new Description("action.butcher.result", corpse.name()));
			builder.add(results);
			return builder.build();
		};

		// Calculate duration based on size of the corpse
		final Duration duration = base.multipliedBy(corpse.size().ordinal());

		// Build response
		return Response.of(new Induction.Instance(induction, duration));
	}
}
