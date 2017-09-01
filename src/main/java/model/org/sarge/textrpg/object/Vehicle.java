package org.sarge.textrpg.object;

import java.util.HashSet;
import java.util.Set;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Parent;
import org.sarge.textrpg.world.Route;

/**
 * Moveable vehicle that can be driven or pushed by an entity, such as a cart, haywain or wheel-barrow.
 * @author Sarge
 * TODO - mount(s) for a waggon, cart, haywain, etc.
 */
public class Vehicle extends WorldObject implements Parent {
	/**
	 * Parent name.
	 */
	public static final String NAME = "vehicle";

	/**
	 * Vehicle descriptor.
	 */
	public static class Descriptor extends ContentsObjectDescriptor {
		protected final Set<Route> routes;
		private final float mod;

		/**
		 * Constructor.
		 * @param descriptor		Vehicle and contents descriptor
		 * @param routes			Valid routes
		 * @param mod				Movement cost modifier
		 * @param surface			Whether this vehicle is a surface (such as a raft) or a container
		 */
		public Descriptor(ContentsObjectDescriptor descriptor, Set<Route> routes, float mod) {
			super(descriptor);
			Check.notNull(routes);
			Check.zeroOrMore(mod);
			this.mod = mod;
			this.routes = new HashSet<>(routes);
		}

		/**
		 * @param route Route-type
		 * @return Whether the given route-type is valid for this vehicle
		 */
		public boolean isValid(Route route) {
			return routes.contains(route);
		}

		/**
		 * @return Movement cost modifier when using this vehicle
		 */
		public float getMovementCostModifier() {
			return mod;
		}

		@Override
		public Vehicle create() {
			return new Vehicle(this);
		}
	}

	private final TrackedContents contents;

	/**
	 * Constructor.
	 * @param descriptor Vehicle descriptor
	 */
	public Vehicle(Descriptor descriptor) {
		super(descriptor);
		this.contents = new TrackedContents(descriptor.limits);
	}

	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.descriptor;
	}

	@Override
	public String getParentName() {
		return NAME;
	}

	@Override
	public Contents getContents() {
		return contents;
	}
}
