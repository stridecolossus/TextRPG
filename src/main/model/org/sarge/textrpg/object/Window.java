package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.View;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;

/**
 * A <i>window</i> can be opened and looked through.
 * @author Sarge
 */
public class Window extends WorldObject implements Openable {
	/**
	 * Window descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final String drape;
		private final View view;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param drape				Drape name, e.g. curtain or shutters
		 * @param view				View from this window
		 */
		public Descriptor(ObjectDescriptor descriptor, String drape, View view) {
			super(descriptor);
			this.drape = notEmpty(drape);
			this.view = notNull(view);
		}

		@Override
		public boolean isFixture() {
			return true;
		}

		@Override
		public boolean isResetable() {
			return true;
		}

		/**
		 * @return Drapes name of this window
		 */
		public String drape() {
			return drape;
		}

		@Override
		public Window create() {
			return new Window(this);
		}
	}

	private final Openable.Model model = new Openable.Model(Openable.Lock.DEFAULT);

	/**
	 * Constructor.
	 * @param descriptor Window descriptor
	 */
	protected Window(Descriptor descriptor) {
		super(descriptor);

	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	@Override
	public String key(boolean carried) {
		return "window";
	}

	@Override
	public Model model() {
		return model;
	}

	/**
	 * @return View from this window
	 */
	public View view() {
		if(model.isOpen()) {
			return this.descriptor().view;
		}
		else {
			return View.NONE;
		}
	}

	@Override
	protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
		assert !carried;
		if(!model.isOpen()) {
			builder.add("window.closed", this.descriptor().drape());
		}
		super.describe(carried, builder, formatters);
	}
}
