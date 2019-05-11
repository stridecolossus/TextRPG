package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.collection.LoopIterator;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ArgumentFormatter.Registry;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Location;

/**
 * Object that can transport passengers such as a ferry, carriage or elevator.
 * @author Sarge
 */
public class Ferry extends WorldObject implements Parent {
	/**
	 * Ferry ticket.
	 * @author Sarge
	 */
	public class Ticket extends WorldObject {
		private final Location dest;

		/**
		 * Constructor.
		 * @param dest 		Ticket destination
		 * @param cost		Ticket cost
		 * @throws IllegalArgumentException if the destination is not a way-point of this ferry
		 * @throws IllegalStateException if this ferry does not require a ticket to travel
		 */
		public Ticket(Location dest, int value) {
			super(ticket(dest, value));
			if(!waypoints.contains(dest)) throw new IllegalArgumentException(String.format("Not a way-point of this ferry: dest=%s ferry=%s", dest.name(), Ferry.this.name()));
			if(!tickets) throw new IllegalStateException("Ferry does not requires a ticket: " + Ferry.this);
			this.dest = notNull(dest);
		}

		/**
		 * @return Ferry for this ticket
		 */
		public Ferry ferry() {
			return Ferry.this;
		}

		/**
		 * @return Destination way-point for this ticket
		 */
		public Location destination() {
			return dest;
		}

		@Override
		protected void describe(boolean carried, Builder builder, Registry formatters) {
			super.describe(carried, builder, formatters);
			if(carried) {
				builder.add("dest", dest.name());
			}
		}
	}

	/**
	 * Creates a ticket descriptor for the given destination.
	 * @param dest		Destination
	 * @param value		Ticket cost
	 * @return Ticket descriptor
	 */
	private static ObjectDescriptor ticket(Location dest, int value) {
		return new ObjectDescriptor.Builder(TextHelper.join("ferry.ticket", dest.name()))
			.value(value)
			.placement("discarded")
			.build();
	}

	private final List<Location> waypoints;
	private final boolean tickets;
	private final Contents contents;

	/**
	 * Constructor.
	 * @param name				Name
	 * @param waypoints			List of way-points
	 * @param tickets			Whether this ferry requires a ticket to travel
	 * @param limits			Passenger limits
	 */
	public Ferry(String name, List<Location> waypoints, boolean tickets, LimitsMap limits) {
		super(new ObjectDescriptor.Builder(name).fixture().placement("here").build());
		if(waypoints.size() < 2) throw new IllegalArgumentException("Ferry must have at least two way-points");
		this.waypoints = List.copyOf(waypoints);
		this.tickets = tickets;
		this.contents = new LimitedContents(limits);
		super.parent(waypoints.get(0));
	}

	@Override
	public Contents contents() {
		return contents;
	}

	/**
	 * @return Whether this ferry requires a ticket to travel
	 */
	public boolean isTicketRequired() {
		return tickets;
	}

	/**
	 * Creates a loop-iterator for this ferry.
	 * @param strategy Iteration policy
	 * @return Loop-iterator
	 */
	public LoopIterator<Location> iterator(LoopIterator.Strategy strategy) {
		return new LoopIterator<>(waypoints, strategy);
	}

	/**
	 * Moves to the given way-point.
	 * @param loc New location
	 */
	public void move(Location loc) {
		// Move to way-point
		super.parent(loc);

		// Notify arrival
		loc.broadcast(null, new Description("ferry.notify.arrival", this.name()));

		// Notify passengers
		// TODO - display location
		Actor.broadcast(null, new Description("ferry.location.move", loc.name()), contents.stream());
	}

	@Override
	public void parent(Parent parent) {
		if(parent != waypoints.get(0)) throw new IllegalStateException("Can only initialise ferry to start way-point");
		super.parent(parent);
	}
}
