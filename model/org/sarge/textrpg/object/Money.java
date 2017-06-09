package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.textrpg.common.Description;

/**
 * Stack of coins.
 * @author Sarge
 */
public class Money extends WorldObject {
	/**
	 * Money descriptor.
	 */
	public static final ObjectDescriptor DESCRIPTOR = new ObjectDescriptor("money");
	
	private final int value;

	/**
	 * Constructor.
	 * @param value Value of this stack of money
	 */
	public Money(int value) {
		super(DESCRIPTOR);
		Check.oneOrMore(value);
		this.value = value;
	}
	
	@Override
	public int getWeight() {
		return value;
	}
	
	@Override
	public int getValue() {
		return value;
	}
	
	@Override
	protected void describe(Description.Builder description) {
		description.add("value", value);
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.equals(this, obj);
	}
}
