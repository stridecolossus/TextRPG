package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.contents.ContentStateChange;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;

/**
 * Container that can be closed such as a chest.
 * @author Sarge
 */
public class OpenableContainer extends Container implements Openable {
	/**
	 * Container descriptor.
	 */
	public static class Descriptor extends Container.Descriptor {
		private final Openable.Lock lock;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param placement			Placement preposition
		 * @param limits			Limits on the contents of this container
		 * @param openable			Lock descriptor for this container
		 */
		public Descriptor(ObjectDescriptor descriptor, String placement, LimitsMap limits, Openable.Lock lock) {
			super(descriptor, placement, limits);
			this.lock = notNull(lock);
		}

		@Override
		public boolean isResetable() {
			return true;
		}

		@Override
		public OpenableContainer create() {
			return new OpenableContainer(this);
		}
	}

	private final Openable.Model model;

	/**
	 * Constructor.
	 * @param descriptor Container descriptor
	 */
	protected OpenableContainer(Descriptor descriptor) {
		super(descriptor);
		model = new Openable.Model(descriptor.lock);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	@Override
	public Model model() {
		return model;
	}

	@Override
	protected boolean isOpen() {
		return model.isOpen();
	}

	@Override
	public boolean notify(ContentStateChange notification) {
		return model.isOpen();
	}
}
