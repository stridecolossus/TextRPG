package org.sarge.textrpg.common;

import org.sarge.lib.object.EqualsBuilder;
import org.sarge.lib.object.ToString;

/**
 * Partial implementation.
 * @author Sarge
 */
public abstract class AbstractNotification implements Notification {
	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.equals(this, that);
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
