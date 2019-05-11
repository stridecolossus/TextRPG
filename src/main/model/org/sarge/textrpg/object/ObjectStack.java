package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;

/**
 * Stack of objects.
 * @author Sarge
 */
public class ObjectStack extends WorldObject {
	private int count;

	/**
	 * Constructor.
	 * @param descriptor		Object descriptor
	 * @param size				Size of this stack
	 * @throws IllegalArgumentException if this object is not {@link ObjectDescriptor#isStackable()}
	 */
	public ObjectStack(ObjectDescriptor descriptor, int size) {
		super(descriptor);
		// TODO - if(!descriptor.isStackable()) throw new IllegalArgumentException("Not stackable: " + descriptor);
		this.count = zeroOrMore(size);
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public int weight() {
		return super.weight() * count;
	}

	@Override
	public int value() {
		throw new UnsupportedOperationException("Object stacks cannot be traded");
	}

	/**
	 * @return Whether this stack is empty
	 */
	public boolean isEmpty() {
		return count == 0;
	}

	@Override
	public boolean isAlive() {
		return !isEmpty() && super.isAlive();
	}

	/**
	 * Modifies the size of this stack.
	 * @param size Size modifier
	 */
	public void modify(int size) {
		if(this.count + size < 0) throw new IllegalArgumentException("Invalid stack increment");
		this.count += size;
	}

	@Override
	protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
		if(carried) {
			builder.add("count", count);
		}
		else {
			builder.add("count", "some");
		}
		super.describe(carried, builder, formatters);
	}

	@Override
	protected void destroy() {
		super.destroy();
		count = 0;
	}
}
