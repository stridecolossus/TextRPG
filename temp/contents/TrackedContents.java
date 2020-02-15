package org.sarge.textrpg.contents;

/**
 * Set of contents that tracks the total weight of its contents.
 * @author Sarge
 */
public class TrackedContents extends Contents {
	private int weight;

	@Override
	public int weight() {
		return weight;
	}

	@Override
	protected void add(Thing thing) {
		super.add(thing);
		weight += thing.weight();
	}

	@Override
	protected void remove(Thing thing) {
		super.remove(thing);
		weight -= thing.weight();
		assert weight >= 0;
	}

	@Override
	protected void update() {
		weight = super.stream().mapToInt(Thing::weight).sum();
	}
}
