package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Descriptor for a coin denomination.
 * <p>
 * Usage:
 * <pre>
 * // Create 'unit' coin
 * final Coin copper = new Coin("copper");
 *
 * // Create higher denominations
 * final Coin silver = new Coin("silver", 100, copper);
 * final Coin gold = new Coin("gold", 20, silver);
 * ...
 * </pre>
 */
public final class Coin extends AbstractEqualsObject implements CommandArgument {
	private final String name;
	private final int value;

	/**
	 * Constructor for the <i>unit</i> denomination coin, i.e. a penny.
	 * @param name Coin name
	 */
	public Coin(String name) {
		this.name = notEmpty(name);
		this.value = 1;
	}

	/**
	 * Constructor for larger denomination coins.
	 * @param name		Coin name
	 * @param value		Value
	 * @param coin		Coin multiplier
	 */
	public Coin(String name, int value, Coin coin) {
		this.name = notEmpty(name);
		this.value = oneOrMore(value) * coin.value;
	}

	/**
	 * @return Coin name
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @return Coin value
	 */
	public int value() {
		return value;
	}
}
