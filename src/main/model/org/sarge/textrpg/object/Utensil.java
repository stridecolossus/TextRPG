package org.sarge.textrpg.object;

import java.util.Map;

import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.LimitedContents;
import org.sarge.textrpg.contents.LimitedContents.Limit;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.contents.Parent;

/**
 * A utensil is an object that can be used to cook a meal.
 * @author Sarge
 */
public class Utensil extends WorldObject implements Parent {
	/**
	 * Ingredient constraint.
	 */
	private static final Limit INGREDIENT = (contents, thing) -> {
		if(thing instanceof Food) {
			final Food food = (Food) thing;
			return food.isCookable();
		}
		else {
			return false;
		}
	};

	/**
	 * Utensil descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final LimitsMap limits;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param capacity			Capacity of this utensil
		 */
		public Descriptor(ObjectDescriptor descriptor, int capacity) {
			super(descriptor);
			this.limits = new LimitsMap(Map.of(
				"utensil.limit.capacity", Limit.capacity(capacity),
				"utensil.limit.size", Limit.size(descriptor.properties().size()),
				"utensil.limit.ingredient", INGREDIENT
			));
		}

		@Override
		public Utensil create() {
			return new Utensil(this);
		}
	}

	private final LimitedContents contents;

	private boolean water;

	/**
	 * Constructor.
	 * @param descriptor Utensil descriptor
	 */
	protected Utensil(Descriptor descriptor) {
		super(descriptor);
		contents = new LimitedContents(descriptor.limits);
	}

	@Override
	public Contents contents() {
		return contents;
	}

	@Override
	public int weight() {
		return super.weight() + contents.weight();
	}

	/**
	 * @return Whether this utensil contains some water for cooking
	 */
	public boolean isWater() {
		return water;
	}

	/**
	 * Sets whether this utensil contains some water for cooking.
	 * @param water Has water
	 */
	void water(boolean water) {
		this.water = water;
	}
}
