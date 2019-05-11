package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Description;

/**
 * Ambient event descriptor.
 */
public final class AmbientEvent extends AbstractEqualsObject {
	private final Duration period;
	private final boolean repeat;
	private final Description description;

	/**
	 * Constructor.
	 * @param name		Name
	 * @param period	Period
	 * @param repeat	Whether this is a repeating event
	 */
	public AmbientEvent(String name, Duration period, boolean repeat) {
		this.period = notNull(period);
		this.repeat = repeat;
		this.description = Description.of(name);
	}

	/**
	 * @return Period
	 */
	public Duration period() {
		return period;
	}

	/**
	 * @return Whether this is a repeating event
	 */
	public boolean isRepeating() {
		return repeat;
	}

	/**
	 * @return Ambient event notification
	 */
	public Description description() {
		return description;
	}
}
