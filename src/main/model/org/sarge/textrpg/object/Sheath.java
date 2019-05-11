package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.Optional;

import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description.Builder;

/**
 * A <i>sheath</i> is a container for a single equipped {@link Weapon} that allows an entity to quickly draw that weapon.
 * @author Sarge
 */
public class Sheath extends WorldObject {
	/**
	 * Sheath descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final String cat;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param cat				Weapon category for this sheath
		 */
		public Descriptor(ObjectDescriptor descriptor, String cat) {
			super(descriptor);
			this.cat = notEmpty(cat);
		}

		@Override
		public Sheath create() {
			return new Sheath(this);
		}
	}

	private Optional<Weapon> weapon = Optional.empty();

	/**
	 * Constructor.
	 * @param descriptor Sheath descriptor
	 */
	protected Sheath(Descriptor descriptor) {
		super(descriptor);
	}

	/**
	 * @return Weapon in this sheath
	 */
	public Optional<Weapon> weapon() {
		return weapon;
	}

	/**
	 * Sheaths a weapon.
	 * @param weapon Weapon
	 * @throws ActionException if this sheath is occupied or the given weapon cannot be sheathed
	 */
	void sheath(Weapon weapon) throws ActionException {
		final Descriptor descriptor = (Descriptor) this.descriptor();
		if(this.weapon.isPresent()) throw ActionException.of("sheath.occupied");
		if(!weapon.isCategory(descriptor.cat)) throw ActionException.of("sheath.invalid.weapon");
		if(this.size().isLessThan(weapon.size())) throw ActionException.of("sheath.too.small");
		this.weapon = Optional.of(weapon);
	}

	/**
	 * Removes the weapon in this sheath.
	 * @throws ActionException if no weapon has been sheathed
	 */
	void clear() throws ActionException {
		if(!weapon.isPresent()) throw ActionException.of("sheath.not.occupied");
		weapon = Optional.empty();
	}

	@Override
	protected void describe(boolean carried, Builder builder, ArgumentFormatter.Registry formatters) {
		super.describe(carried, builder, formatters);
		if(carried) {
			weapon.map(Weapon::name).ifPresent(name -> builder.add("sheath.weapon", name));
		}
	}
}
