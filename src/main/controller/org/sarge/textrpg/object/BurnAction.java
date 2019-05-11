package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Optional;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.Corpse;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to burn an object or corpse.
 * @author Sarge
 */
@Component
public class BurnAction extends AbstractAction {
	private static final WorldObject.Filter FIREWOOD = WorldObject.Filter.of("firewood");
	private static final ObjectDescriptor PYRE = ObjectDescriptor.fixture("pyre");

	private final ObjectController controller;
	private final Duration duration;
	private final Duration lifetime;

	/**
	 * Constructor.
	 * @param duration		Pyre build duration
	 * @param lifetime		Pyre lifetime
	 * @param controller	Controller for pyre decay events
	 */
	public BurnAction(@Value("${pyre.build.duration}") Duration duration, @Value("${pyre.base.lifetime}") Duration lifetime, ObjectController controller) {
		super(Flag.REVEALS, Flag.INDUCTION, Flag.BROADCAST);
		this.duration = notNull(duration);
		this.lifetime = notNull(lifetime);
		this.controller = notNull(controller);
	}

	/**
	 * Burns a flammable object.
	 * @param obj Object to burn
	 * @return Response
	 * @throws ActionException if the object cannot be burnt
	 */
	@RequiredObject(Light.TINDERBOX)
	public Response burn(WorldObject obj) throws ActionException {
		final var mat = obj.descriptor().characteristics().material();
		if(!mat.isDamagedBy(Damage.Type.FIRE) || (mat.strength() > 1)) throw ActionException.of("burn.cannot.burn");
		obj.destroy();
		return AbstractAction.response("action.burn", obj.name());
	}

	/**
	 * Pyre object.
	 */
	private class Pyre extends WorldObject implements Parent {
		// Pyre contents
		private final Contents contents = new Contents() {
			@Override
			public Optional<String> reason(Thing thing) {
				return Optional.of("pyre.cannot.add");
			}

			@Override
			public Contents.EnumerationPolicy policy() {
				if(active) {
					return Contents.EnumerationPolicy.NONE;
				}
				else {
					return Contents.EnumerationPolicy.DEFAULT;
				}
			}
		};

		private final Percentile smoke;

		private boolean active = true;

		/**
		 * Constructor.
		 * @param size Size of this pyre
		 */
		public Pyre(int size) {
			super(PYRE);
			this.smoke = Percentile.of(size * 20);
		}

		@Override
		public Contents contents() {
			return contents;
		}

		@Override
		public Percentile emission(Emission emission) {
			if(active) {
				switch(emission) {
				case LIGHT:		return Percentile.ONE;
				case SMOKE:		return smoke;
				}
			}

			return super.emission(emission);
		}
	}

	/**
	 * Builds a pyre for a corpse.
	 * @param actor			Actor
	 * @param corpse		Corpse
	 * @return Response
	 * @throws ActionException if the actor does not have sufficient wood
	 */
	@RequiresActor
	@RequiredObject(Light.TINDERBOX)
	public Response pyre(Entity actor, Corpse corpse) throws ActionException {
		// Check fire-wood
		final int size = corpse.size().ordinal();
		final var wood = actor.contents().select(WorldObject.class).filter(FIREWOOD).limit(size).collect(toList());
		if(wood.size() < size) throw ActionException.of("pyre.insufficient.wood");

		// Create pyre induction
		final Induction induction = () -> {
			// Check can still build pyre
			if(!corpse.isAlive()) throw ActionException.of("pyre.requires.corpse");

			// Create pyre
			final Pyre pyre = new Pyre(size);
			corpse.destroy();
			wood.forEach(WorldObject::destroy);

			// Register expiry event
			final Duration lifetime = this.lifetime.multipliedBy(size);
			final Event expiry = () -> {
				// Extinguish pyre
				pyre.active = false;

				// Add corpse contents
				// TODO - helper
				corpse.contents().stream().forEach(t -> t.parent(pyre));

				// Register decay
				// TODO
				// 1. re-uses lifetime for decay
				// 2. PYRE does not have decay duration
				// 3. no notifications
				final Event event = () -> {
					pyre.destroy();
					return false;
				};
				controller.register(event, lifetime);

				return false;
			};
			controller.register(expiry, lifetime);

			// Build response
			return AbstractAction.response("action.pyre", corpse.name());
		};

		// Build response
		return Response.of(new Induction.Instance(induction, duration));
	}
}
