package org.sarge.textrpg.entity;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Randomiser;

/**
 * Effect that is randomly applied.
 * @author Sarge
 */
public class ChanceEffectMethod implements EffectMethod {
	private final EffectMethod delegate;
	private final Percentile chance;

	/**
	 * Constructor.
	 * @param delegate		Delegate effect
	 * @param chance		Chance to apply
	 */
	public ChanceEffectMethod(EffectMethod delegate, Percentile chance) {
		Check.notNull(delegate);
		Check.notNull(chance);
		this.delegate = delegate;
		this.chance = chance;
	}

	@Override
	public void apply(Entity e, int size) {
		if(size > 0) {
			if(Randomiser.percentile(chance)) {
				delegate.apply(e, size);
			}
		}
		else {
			delegate.apply(e, size);
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
