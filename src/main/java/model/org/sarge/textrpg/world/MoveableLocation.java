package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.util.CycleIterator;
import org.sarge.textrpg.world.LinkWrapper.ReversePolicy;

/**
 * Moveable location such as a ferry, elevator or carriage.
 * @author Sarge
 */
public class MoveableLocation extends Location {
	/**
	 * Event queue for location transitions.
	 */
	protected static final EventQueue QUEUE = new EventQueue();

	/**
	 * Stage wrapper.
	 */
	public static class Stage extends Location {
		private final long period;

		private boolean open;

		/**
		 * Constructor.
		 * @param loc		Stage location
		 * @param period	Period (ms)
		 */
		public Stage(Location loc, long period) {
			super(loc);
			Check.oneOrMore(period);
			this.period = period;
		}

		@Override
		public void add(LinkWrapper wrapper) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isOpen() {
			return open;
		}

		@Override
		public Map<Direction, Exit> getExits() {
			if(open)
				return super.getExits();
			else
				return Collections.emptyMap();
		}

		/**
		 * Broadcasts arrival/departure notifications to neighbouring locations.
		 * @param arrival	Arrival or departure
		 * @param name		Name key
		 */
		private void broadcast(boolean arrival, String name) {
			final Notification n = new Message(name + "." + (arrival ? "arrived" : "departure"));
			getExits().values().stream().map(Exit::getDestination).forEach(loc -> loc.broadcast(Actor.SYSTEM, n));
		}
	}

	private final CycleIterator<Stage> itr;
	private final boolean ferry;

	/**
	 * Constructor.
	 * @param loc		This location
	 * @param stages	Stages in the journey of this location
	 * @param ferry		Whether this is a ferry (over-rides area/name attributes)
	 * @see MoveableLocation#describe(org.sarge.textrpg.common.Description.Builder)
	 */
	public MoveableLocation(String name, List<Stage> stages, boolean ferry) {
		super(name, stages.get(0));
		this.ferry = ferry;
		this.itr = new CycleIterator<>(stages);
		this.itr.next().open = true;
		next();
	}

	@Override
	public void add(LinkWrapper wrapper) {
		throw new UnsupportedOperationException("Cannot link from a moveable location");
	}

	@Override
	protected Link invert(Link link, ReversePolicy reverse) {
		throw new UnsupportedOperationException("Cannot link to a moveable location");
	}

	/**
	 * Registers event to move to next stage.
	 */
	private void next() {
		final Runnable event = () -> {
			move();
			next();
		};
		QUEUE.add(event, itr.current().period);
	}

	/**
	 * Moves to the next stage.
	 */
	protected void move() {
		// Notify departure to neighbours
		final Stage prev = itr.current();
		prev.broadcast(false, getName());

		// Close
		assert prev.open;
		prev.open = false;

		// Move to next stage
		final Stage next = itr.next();

		// Move contents
		final List<Thing> contents = new ArrayList<>(prev.contents.stream().collect(toList()));
		contents.stream().forEach(t -> t.setParentAncestor(next));

		// Open
		assert !next.open;
		next.open = true;

		// Notify arrival to neighbours
		next.broadcast(true, getName());

		// Notify passengers
		contents.stream().filter(Thing::isSentient).forEach(t -> describe(next, (Actor) t));
	}

	/**
	 * Helper - Describes location transitions to passengers.
	 */
	private static void describe(Location loc, Actor actor) {
		final Description description = loc.describe(true, actor);
		actor.alert(description.toNotification());
	}

	/**
	 * @return Current location
	 */
	protected Stage getLocation() {
		return itr.current();
	}

	@Override
	public Area getArea() {
		return getLocation().getArea();
	}

	@Override
	public Terrain getTerrain() {
		return getLocation().getTerrain();
	}

	@Override
	public boolean isProperty(Property p) {
	    return getLocation().isProperty(p);
	}

	@Override
	public Map<Direction, Exit> getExits() {
		return getLocation().getExits();
	}

	@Override
	protected void describe(Description.Builder builder) {
		if(ferry) {
			builder.wrap("name", getLocation().getName());
			builder.wrap("area", super.getName());
		}
		else {
			super.describe(builder);
		}
	}
}
