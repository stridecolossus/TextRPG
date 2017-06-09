package org.sarge.textrpg.object;

/**
 * Descriptor for a fixed object.
 * @author Sarge
 */
public class Fixture extends ObjectDescriptor {
	public Fixture(ObjectDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	
	@Override
	public WorldObject create() {
		return new WorldObject(this) {
			@Override
			protected String getFullDescriptionKey() {
				final String key = super.getFullDescriptionKey();
				if(key.equals(ObjectDescriptor.DEFAULT_DESCRIPTION)) {
					return "stands";
				}
				else {
					return key;
				}
			}
		};
	}
}
