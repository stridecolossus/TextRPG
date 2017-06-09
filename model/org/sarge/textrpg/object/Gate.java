package org.sarge.textrpg.object;

import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Parent;

/**
 * Town gate.
 * @author Sarge
 */
public class Gate extends Portal {
	/**
	 * Constructor.
	 * @param descriptor	Portal descriptor
	 * @param dest			Other side of this gate
	 */
	public Gate(Portal.Descriptor descriptor, Parent dest) {
		super(descriptor, dest);
	}
	
	@Override
	public boolean isFixture() {
		return true;
	}
	
	@Override
	protected String getFullDescriptionKey() {
		return "stands";
	}
	
	/**
	 * Call for this gate to be opened or closed.
	 */
	protected void call() {
		model.get().toggle();
	}
	
	public boolean isOpen() {
		return model.get().isOpen();
	}

	/**
	 * Resets this gate.
	 * @param open Open or closed
	 */
	public void reset(boolean open) {
		// Toggle gate
		final Openable model = super.model.get();
		if(model.isOpen() != open) {
			model.toggle();
		}
		
		// Clear current event
		model.getEventHolder().cancel();
	}
}
