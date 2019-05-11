package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Percentile;

/**
 * A <i>trap</i> defines a trapped portal or container.
 * @author Sarge
 */
public final class Trap extends AbstractEqualsObject implements Hidden {
	private final Effect effect;
	private final Percentile diff;

	/**
	 * Constructor.
	 * @param effect		Trap effect(s)
	 * @param diff			Detect/disarm difficulty
	 */
	public Trap(Effect effect, Percentile diff) {
		this.effect = notNull(effect);
		this.diff = notNull(diff);
	}

	/**
	 * @return Trap effect(s)
	 */
	public Effect effects() {
		return effect;
	}

	/**
	 * @return Detect/disarm difficulty
	 */
	public Percentile difficulty() {
		return diff;
	}

	@Override
	public Percentile visibility() {
		throw new UnsupportedOperationException("Trap is a hidden for detect/disarm purposes only");
	}
}
