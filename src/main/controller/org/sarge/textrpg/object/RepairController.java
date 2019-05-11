package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;

import org.sarge.textrpg.util.BandingTable;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Percentile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Repair controller.
 * @author Sarge
 */
@Controller
public class RepairController {
	private final Event.Queue queue;

	private int cost = 1;
	private Duration duration = Duration.ofMinutes(1);
	private Duration expiry = Duration.ofHours(1);
	private Function<Percentile, String> mapper = ignore -> "shortly";

	/**
	 * Constructor.
	 * @param manager Queue manager for repair events
	 */
	public RepairController(Event.Queue.Manager manager) {
		this.queue = manager.queue("queue.repair");
	}

	/**
	 * Sets the cost of repairs (per unit-of-wear).
	 * @param cost Repair cost
	 */
	@Autowired
	public void setCost(@Value("${repair.cost}") int cost) {
		this.cost = oneOrMore(cost);
	}

	/**
	 * Sets the duration of repairs (per unit-of-wear).
	 * @param duration Repair duration
	 */
	@Autowired
	public void setDuration(@Value("${repair.duration}") Duration duration) {
		this.duration = DurationConverter.oneOrMore(duration);
	}

	/**
	 * Sets the expiry duration for unclaimed objects.
	 * @param expiry Expiry duration
	 */
	@Autowired
	public void setExpiry(@Value("${repair.expiry}") Duration expiry) {
		this.expiry = DurationConverter.oneOrMore(expiry);
	}

	/**
	 * Sets the banding table that maps object condition to a description.
	 * @param table Banding table
	 */
	@Autowired
	public void setMapper(@Value("#{banding.table('repair.duration')}") BandingTable<Percentile> table) {
		this.mapper = table::map;
	}

	/**
	 * @return Cost per unit-of-wear
	 */
	public int cost() {
		return cost;
	}

	/**
	 * Determines the repair duration description for the given object.
	 * @param obj Durable object
	 * @return Repair duration description
	 */
	public String description(DurableObject obj) {
		return mapper.apply(obj.condition());
	}

	/**
	 * Starts repairing the given object.
	 * @param obj			Object to repair
	 * @param pending		Store for repaired objects
	 */
	public void repair(DurableObject obj, Collection<WorldObject> pending) {
		// Create repair callback
		final Event repair = () -> {
			// Repair and move to pending store
			obj.repair();
			pending.add(obj);

			// Register expiry event
			final Event expire = () -> pending.remove(obj);
			queue.add(expire, expiry);

			return false;
		};

		// Register repair callback depending on amount of damage
		final int wear = Math.max(1, obj.wear());
		queue.add(repair, duration.multipliedBy(wear));
	}
}
