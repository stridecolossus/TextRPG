package org.sarge.textrpg.entity;

import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Description;

/**
 * Call-back for an action that has an induction.
 * @author Sarge
 */
@FunctionalInterface
public interface Induction {
	/**
	 * Completes this induction.
	 * @returns Induction description
	 * @throws ActionException if this induction cannot be completed
	 */
	Description complete() throws ActionException;

	/**
	 * Interrupts this induction (default does nothing).
	 */
	default void interrupt() {
		// Does nowt
	}
	
	/**
	 * Call-back listener on this induction.
	 */
	@FunctionalInterface
	interface Listener {
		/**
		 * Notifies a completed induction.
		 * @param completed Whether the induction completed normally or was interrupted
		 */
		void notify(boolean completed);
	}
}
