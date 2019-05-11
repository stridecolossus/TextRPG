package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.textrpg.util.ArgumentFormatter.Registry;
import org.sarge.textrpg.util.Description.Builder;
import org.sarge.textrpg.util.TextHelper;

/**
 * Food that can be eaten and also used in cooking.
 * @author Sarge
 */
public class Food extends WorldObject {
	/**
	 * Food descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		/**
		 * Food filter.
		 */
		public static final ObjectDescriptor.Filter FILTER = desc -> desc instanceof Descriptor;

		private final int nutrition;
		private final boolean meat;
		private final Descriptor cooked;

		/**
		 * Constructor for cooked food.
		 * @param descriptor		Object descriptor
		 * @param nutrition			Nutrition level
		 */
		public Descriptor(ObjectDescriptor descriptor, int nutrition) {
			super(descriptor);
			this.nutrition = oneOrMore(nutrition);
			this.meat = false;
			this.cooked = null;
		}

		/**
		 * Constructor for raw food.
		 * @param descriptor		Object descriptor
		 * @param meat				Whether this food is raw meat
		 * @param nutrition			Nutrition level
		 * @param cook				Cooking nutrition modifier
		 */
		public Descriptor(ObjectDescriptor descriptor, boolean meat, int nutrition, float cook) {
			super(descriptor);
			this.nutrition = oneOrMore(nutrition);
			this.meat = meat;
			this.cooked = create(descriptor, (int) (nutrition * cook));
		}

		/**
		 * Creates cooked food descriptor.
		 * @param descriptor		Raw food descriptor
		 * @param nutrition			Nutrition level
		 * @return Cooked descriptor
		 */
		private static Descriptor create(ObjectDescriptor descriptor, int nutrition) {
			final String name = TextHelper.join("cooked", descriptor.name());
			final ObjectDescriptor cooked = new ObjectDescriptor(name, descriptor);
			return new Descriptor(cooked, nutrition);
		}

		@Override
		public boolean isPerishable() {
			return true;
		}

		/**
		 * @return Nutrition provided by this food
		 */
		public int nutrition() {
			return nutrition;
		}

		@Override
		public Food create() {
			if(meat) {
				return new Meat(this);
			}
			else {
				return new Food(this);
			}
		}
	}

	/**
	 * Meat that decays to rotten meat.
	 */
	private static class Meat extends Food {
		private boolean rotten;

		/**
		 * Constructor.
		 * @param descriptor Food descriptor
		 */
		private Meat(Descriptor descriptor) {
			super(descriptor);
		}

		@Override
		public String name() {
			if(rotten) {
				return "rotten.meat";
			}
			else {
				return super.name();
			}
		}

		@Override
		protected boolean decay() {
			if(rotten) {
				return super.decay();
			}
			else {
				rotten = true;
				return true;
			}
		}

		@Override
		public boolean isCookable() {
			return !rotten;
		}

		@Override
		protected Food cook() {
			assert !rotten;
			return super.cook();
		}

		@Override
		protected void describe(boolean carried, Builder builder, Registry formatters) {
			if(rotten) {
				builder.add(KEY_STATE, "food.rotten");
			}
			super.describe(carried, builder, formatters);
		}
	}

	/**
	 * Constructor.
	 * @param descriptor Food descriptor
	 */
	protected Food(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}

	/**
	 * @return Whether this food can be cooked
	 */
	public boolean isCookable() {
		final Descriptor descriptor = this.descriptor();
		return descriptor.cooked != null;
	}

	/**
	 * Cooks this food.
	 * @return Cooked food
	 * @throws IllegalStateException if this food is already cooked
	 */
	protected Food cook() {
		final Descriptor descriptor = this.descriptor();
		if(!isCookable()) throw new IllegalStateException("Cannot cook food: " + this);
		destroy();
		return descriptor.cooked.create();
	}
}
