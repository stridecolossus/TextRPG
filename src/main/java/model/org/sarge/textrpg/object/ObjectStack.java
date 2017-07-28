package org.sarge.textrpg.object;

import org.sarge.lib.util.Check;

/**
 * Stack of objects, e.g. arrows.
 * @author Sarge
 */
public class ObjectStack extends WorldObject {
	private int size;

	/**
	 * Constructor.
	 * @param descriptor		Object descriptor
	 * @param size				Stack size (two-or-more)
	 * @throws IllegalArgumentException if the descriptor is not for a basic object
	 */
	public ObjectStack(ObjectDescriptor descriptor, int size) {
		super(descriptor);
		if(descriptor.getClass() != ObjectDescriptor.class) throw new IllegalArgumentException("Stacks can only comprise basic objects");
		if(descriptor.getProperties().getResetPeriod() > 0) throw new IllegalArgumentException("Cannot register reset events on a stack");
		if(descriptor.getProperties().getForgetPeriod() > 0) throw new IllegalArgumentException("Stacks cannot be hidden");
		if(size < 2) throw new IllegalArgumentException("Stack must be two-or-more");
		this.size = size;
	}
	
	/**
	 * @return Stack size
	 */
	public int size() {
		return size;
	}

	/**
	 * Increments this stack.
	 * @param size Stack increment
	 */
	protected void add(int size) {
		Check.oneOrMore(size);
		this.size += size;
	}
	
	/**
	 * Pops an object from this stack.
	 * @return New object
	 */
	protected WorldObject pop() {
		assert size > 0;
		if(size == 1) {
			return this;
		}
		else {
			--size;
			return descriptor.create();
		}
	}
}
