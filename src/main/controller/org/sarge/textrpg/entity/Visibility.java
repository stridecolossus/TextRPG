package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.util.Percentile;

/**
 * Visibility model for an entity.
 * @author Sarge
 */
public class Visibility extends AbstractEqualsObject {
	private Percentile stance;
	private int mod;
	private Percentile vis = Percentile.ONE;

	/**
	 * @return Visibility
	 */
	public Percentile get() {
		if(vis == null) {
			final int base = stance == null ? Percentile.MAX : stance.intValue();
			final int total = mod + base;
			final int clamped = Util.clamp(total, 0, Percentile.MAX);
			vis = Percentile.of(clamped);
		}

		return vis;
	}

	/**
	 * Applies the stance modifier.
	 * @param mod Modifier
	 * @throws IllegalStateException if a stance modifier has already been applied
	 * @see #remove()
	 */
	public void stance(Percentile stance) {
		if(this.stance != null) throw new IllegalStateException("Stance modifier already applied");
		this.stance = notNull(stance);
		dirty();
	}

	/**
	 * Removes the stance modifier.
	 * @throws IllegalStateException if a stance modifier has not been applied
	 * @see #stance(Percentile)
	 */
	public void remove() {
		if(this.stance == null) throw new IllegalStateException("Stance modifier not applied");
		stance = null;
		dirty();
	}

	/**
	 * Applies additional visibility modifier(s).
	 * Note that the modifier is inverted.
	 * @param mod Modifier
	 */
	public void modifier(int mod) {
		this.mod = -mod;
		dirty();
	}

	/**
	 * Marks the visibility as modified.
	 */
	private void dirty() {
		vis = null;
	}
}
