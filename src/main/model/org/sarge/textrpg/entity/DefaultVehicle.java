package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;

import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.entity.Vehicle.AbstractVehicle;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;
import org.sarge.textrpg.world.Trail;

/**
 * Vehicle that can be driven such as a carriage or cart.
 * @author Sarge
 * TODO - mount(s) required, driver is first (or only) passenger
 */
public class DefaultVehicle extends AbstractVehicle {
	/**
	 * Vehicle descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final LimitsMap limits;
		private final Set<Terrain> terrain;
		private final Set<Route> routes;
		private final Percentile noise;

		/**
		 * Constructor.
		 * @param descriptor		Vehicle descriptor
		 * @param limits			Contents limits
		 * @param terrain			Terrain(s) that this vehicle can traverse
		 * @param routes			Route(s) that this vehicle can traverse
		 * @throws IllegalArgumentException if the limits or empty or either the terrain or route constraints are empty
		 */
		public Descriptor(ObjectDescriptor descriptor, LimitsMap limits, Set<Terrain> terrain, Set<Route> routes, Percentile noise) {
			super(descriptor);
			this.limits = notNull(limits);
			this.terrain = Set.copyOf(notEmpty(terrain));
			this.routes = Set.copyOf(notEmpty(routes));
			this.noise = notNull(noise);
		}

		@Override
		public DefaultVehicle create() {
			return new DefaultVehicle(this);
		}

		@Override
		public boolean isFixture() {
			return true;
		}
	}

	private final Trail trail = new Trail();

	/**
	 * Constructor.
	 * @param descriptor Vehicle descriptor
	 */
	public DefaultVehicle(Descriptor descriptor) {
		super(descriptor, descriptor.limits, Contents.PLACEMENT_DEFAULT);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	@Override
	public boolean isRaft() {
		return false;
	}

	@Override
	public Percentile noise() {
		return descriptor().noise;
	}

	@Override
	public boolean isValid(Exit exit) {
		final Descriptor descriptor = this.descriptor();
		if(!descriptor.terrain.contains(exit.destination().terrain())) return false;
		if(!descriptor.routes.contains(exit.link().route())) return false;
		return true;
	}

	@Override
	protected Trail trail() {
		return trail;
	}

	@Override
	public Percentile tracks() {
		return Percentile.ONE;
	}
}
