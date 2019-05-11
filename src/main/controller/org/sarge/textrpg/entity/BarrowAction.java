package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Optional;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Terrain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to bury a corpse or dig up a barrow.
 * @author Sarge
 */
@Component
public class BarrowAction extends AbstractAction {
	/**
	 * Barrow instance.
	 */
	final class Barrow extends WorldObject implements Parent {
		private final Contents contents = new Contents() {
			@Override
			public Contents.EnumerationPolicy policy() {
				if(excavated) {
					return Contents.EnumerationPolicy.DEFAULT;
				}
				else {
					return Contents.EnumerationPolicy.NONE;
				}
			}
		};

		private boolean excavated;

		/**
		 * Constructor.
		 */
		private Barrow() {
			super(descriptor);
		}

		/**
		 * @return Whether this barrow has been excavated
		 */
		boolean isExcavated() {
			return excavated;
		}

		@Override
		public Contents contents() {
			return contents;
		}
	}

	private final Duration duration;
	private final ObjectDescriptor descriptor;

	/**
	 * Constructor.
	 * @param duration		Base build duration
	 * @param decay			Decay period for a barrow
	 */
	public BarrowAction(@Value("${barrow.build.duration}") Duration duration, @Value("${barrow.decay}") Duration decay) {
		super(Flag.LIGHT, Flag.REVEALS, Flag.INDUCTION, Flag.BROADCAST);
		this.duration = notNull(duration);
		this.descriptor = new ObjectDescriptor.Builder("barrow").fixture().placement("stands").decay(decay).build();
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		switch(terrain) {
		case FARMLAND:
		case MARSH:
		case ICE:
		case URBAN:
		case INDOORS:
		case DARK:
		case UNDERGROUND:
		case WATER:
			return false;

		default:
			return true;
		}
	}

	/**
	 * Buries the given corpse and creates a {@link Barrow}.
	 * @param actor			Actor
	 * @param corpse		Corpse to bury
	 * @return Response
	 * @throws ActionException if there is already a barrow in this location
	 */
	@RequiresActor
	@RequiredObject("shovel")
	public Response barrow(Entity actor, Corpse corpse) throws ActionException {
		// Check no barrow already in location
		final Location loc = actor.location();
		if(find(loc).isPresent()) throw ActionException.of("barrow.already.present");

		// Create barrow induction
		final Induction induction = () -> {
			// Check corpse not decayed during induction
			if(!corpse.isAlive()) throw ActionException.of("barrow.requires.corpse");

			// Create barrow
			final Barrow barrow = new Barrow();
			barrow.parent(loc);
			// TODO - ObjectController::decay

			// Destroy corpse and bury contents
			corpse.contents().move(barrow);
			corpse.destroy();

			// Build response
			return AbstractAction.response("action.barrow", corpse.name());
		};

		// Build response
		// TODO - duration ~ size
		return Response.of(new Induction.Instance(induction, duration));
	}

	/**
	 * Excavates the barrow in the current location.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if there is no barrow or it has already been excavated
	 */
	@RequiresActor
	@RequiredObject("shovel")
	public Response excavate(Entity actor) throws ActionException {
		// Find barrow
		final Location loc = actor.location();
		final Barrow barrow = find(loc).orElseThrow(() -> ActionException.of("excavate.requires.barrow"));
		if(barrow.excavated) throw ActionException.of("excavate.already.excavated");

		// Create excavate induction
		final Induction induction = () -> {
			if(!barrow.isAlive() || barrow.excavated) throw ActionException.of("excavate.barrow.decayed");
			barrow.excavated = true;
			return Response.OK;
		};

		// Build response
		return Response.of(new Induction.Instance(induction, duration));
	}

	/**
	 * Helper - Finds a barrow in the current location.
	 */
	private static Optional<Barrow> find(Location loc) {
		return loc.contents().select(Barrow.class).findAny();
	}
}
