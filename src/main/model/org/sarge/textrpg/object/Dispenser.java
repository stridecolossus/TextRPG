package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;

/**
 * A <i>dispenser</i> is an adapter for a descriptor that generates a number of dispensed objects, e.g. a tree containing apples, a forgotten map, etc.
 * @author Sarge
 */
public class Dispenser extends ObjectDescriptor {
	private final ObjectDescriptor descriptor;
	private final int max;
	private final Duration refresh;

	private int count;			// Number of dispensed objects

	/**
	 * Constructor.
	 * @param descriptor		Descriptor for dispensed objects
	 * @param max				Initial/maximum number of objects to dispense
	 * @param refresh			Refresh period
	 */
	public Dispenser(ObjectDescriptor descriptor, int max, Duration refresh) {
		super(descriptor);
		if(descriptor.isFixture()) throw new IllegalArgumentException("Cannot dispense fixtures");
		this.descriptor = notNull(descriptor);
		this.max = oneOrMore(max);
		this.refresh = notNull(refresh);
	}

	/**
	 * @return Initial/maximum number of objects to dispense
	 */
	public int maximum() {
		return max;
	}

	/**
	 * @return Refresh period
	 */
	public Duration refresh() {
		return refresh;
	}

	/**
	 * Dispenses an object.
	 * @throws IllegalStateException if all objects have been dispensed
	 */
	public void dispense() {
		if(count >= max) throw new IllegalStateException("Dispenser limit reached: " + this);
		++count;
	}

	/**
	 * Restores a dispensed object.
	 * @throws IllegalStateException if no objects have been dispensed
	 */
	public void restore() {
		if(count == 0) throw new IllegalStateException("No objects have been dispensed: " + this);
		--count;
	}

	@Override
	public WorldObject create() {
		return descriptor.create();
	}
}
