package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ValueModifier;

/**
 * A <i>transaction</i> is a pending modifier to an {@link EntityValue}.
 * @author Sarge
 */
public final class Transaction extends AbstractEqualsObject {
	private final ValueModifier mod;
	private final int size;
	private final String message;

	private boolean completed;

	/**
	 * Constructor.
	 * @param mod			Modifier
	 * @param size			Modification size
	 * @param message		Exception message
	 */
	public Transaction(ValueModifier mod, int size, String message) {
		this.mod = notNull(mod);
		this.size = oneOrMore(size);
		this.message = notEmpty(message);
	}


	/**
	 * @return Whether this transaction can be completed
	 */
	public boolean isValid() {
		return mod.get() >= size;
	}

	/**
	 * @throws ActionException if this transaction cannot be completed
	 */
	public void check() throws ActionException {
		if(!isValid()) {
			throw ActionException.of(message);
		}
	}

	/**
	 * Completes this transaction.
	 * @throws IllegalStateException if the transaction cannot be completed
	 */
	public void complete() {
		if(!isValid()) throw new IllegalStateException("Cannot complete: " + this);
		if(completed) throw new IllegalStateException("Already completed");
		mod.modify(-size);
		completed = true;
	}
}
