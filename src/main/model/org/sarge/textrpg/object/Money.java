package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.oneOrMore;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.ArgumentFormatter.Registry;
import org.sarge.textrpg.util.Description.Builder;

/**
 * Stack of coins.
 * @author Sarge
 */
public final class Money extends WorldObject {
	/**
	 * Money descriptor.
	 */
	private static final ObjectDescriptor DESCRIPTOR = ObjectDescriptor.of("money");

	private final int amount;

	/**
	 * Constructor.
	 * @param amount Monetary value
	 */
	public Money(int amount) {
		super(DESCRIPTOR);
		this.amount = oneOrMore(amount);
	}

	@Override
	public int value() {
		return amount;
	}

	@Override
	public int weight() {
		return amount;
	}

	@Override
	protected void describe(boolean carried, Builder builder, Registry formatters) {
		super.describe(carried, builder, formatters);
		builder.add("amount", amount, formatters.get(ArgumentFormatter.MONEY));
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that);
	}
}
