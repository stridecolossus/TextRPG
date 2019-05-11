package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.entity.EntityValue.Key;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.ValueModifier;

/**
 * Set of entity-values that also enforces maximum and minimum values.
 */
public class EntityValueIntegerMap extends MutableIntegerMap<EntityValue.Key> {
	/**
	 * Default entry that clamps to zero.
	 */
	public static class ClampedEntry extends DefaultEntry {
		@Override
		public int get() {
			return Math.max(0, super.get());
		}
	}

	/**
	 * Entry that enforces positive values.
	 */
	public static class PositiveEntry extends DefaultEntry {
		@Override
		public void set(int value) {
			if(value < 0) throw new IllegalArgumentException("Value must be positive");
			super.set(value);
		}

		@Override
		public int modify(float inc) {
			if(get() + inc < 0) throw new IllegalArgumentException("Value must be positive");
			return super.modify(inc);
		}
	}

	/**
	 * Entry that clamps to a percentile value.
	 */
	public static class PercentileEntry extends PositiveEntry {
		@Override
		public void set(int value) {
			super.set(value);
			clamp(value);
		}

		@Override
		public int modify(float inc) {
			final int value = super.modify(inc);
			clamp(value);
			return super.get();
		}

		/**
		 * Clamps percentile values.
		 * @param value New value
		 */
		private void clamp(int value) {
			if(value > Percentile.MAX) {
				super.set(Percentile.MAX);
			}
		}
	}

	/**
	 * Visibility entry.
	 */
	private static class VisibilityEntry implements MutableEntry {
		private final Visibility model = new Visibility();

		@Override
		public int get() {
			return model.get().intValue();
		}

		@Override
		public void set(int value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int modify(float value) {
			model.modifier((int) value);
			return get();
		}
	}

	/**
	 * Primary entry.
	 * <p>
	 * A <i>primary</i> entry is comprised of the main value stored as a floating-point and the associated maximum.
	 * <p>
	 * The value is clamped to zero and the associated maximum.
	 * Setting this value also sets the associated maximum.
	 */
	public static class PrimaryEntry implements MutableEntry {
		private final DefaultEntry max;
		private float value;

		/**
		 * Constructor.
		 * @param max Maximum entry
		 */
		public PrimaryEntry(DefaultEntry max) {
			this.max = notNull(max);
		}

		@Override
		public int get() {
			return (int) value;
		}

		private int max() {
			return max.get();
		}

		@Override
		public void set(int value) {
			Check.zeroOrMore(value);
			max.set(value);
			this.value = value;
		}

		@Override
		public int modify(float inc) {
			value = Util.clamp(value + inc, 0, max());
			return (int) value;
		}
	}

	private final VisibilityEntry vis = new VisibilityEntry();

	/**
	 * Constructor.
	 */
	public EntityValueIntegerMap() {
		for(EntityValue value : EntityValue.PRIMARY_VALUES) {
			final PositiveEntry max = new PositiveEntry();
			map.put(value.key(), new PrimaryEntry(max));
			map.put(value.key(Key.Type.MAXIMUM), max);
			map.put(value.key(Key.Type.REGENERATION), new PositiveEntry());
		}
		map.put(EntityValue.VISIBILITY.key(), vis);
	}

	@Override
	protected MutableEntry create(Key key) {
		switch(key.value().type()) {
		case PRIMARY:
		case VISIBILITY:
			throw new RuntimeException();

		case PERCENTILE:
			return new PercentileEntry();

		case POSITIVE:
			return new PositiveEntry();

		default:
			return new ClampedEntry();
		}
	}

	/**
	 * @return Visibility model
	 */
	public Visibility visibility() {
		return vis.model;
	}

	/**
	 * Creates an entity-value transaction.
	 * @param value
	 * @param mod			Modifier
	 * @param message		Exception message
	 * @throws IllegalArgumentException if the given value is not {@link EntityValue#STAMINA} or {@link EntityValue#POWER}
	 */
	public Transaction transaction(EntityValue value, int mod, String message) {
		if((value != EntityValue.STAMINA) && (value != EntityValue.POWER)) throw new IllegalArgumentException("Invalid transaction type: " + value);
		final ValueModifier entry = get(value.key());
		return new Transaction(entry, mod, message);
	}
}
