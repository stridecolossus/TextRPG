package org.sarge.textrpg.contents;

/**
 * Contents for objects that are in <b>limbo</b>.
 * @author Sarge
 */
final class LimboContents extends Contents {
	@Override
	public void add(Thing thing) {
		throw new UnsupportedOperationException("Cannot add an object to limbo: " + thing);
	}

	@Override
	protected void remove(Thing thing) {
		// Ignored
	}
}
