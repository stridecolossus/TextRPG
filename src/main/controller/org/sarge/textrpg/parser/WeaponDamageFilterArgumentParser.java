package org.sarge.textrpg.parser;

import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Filter;
import org.sarge.textrpg.util.WordCursor;
import org.sarge.textrpg.object.Weapon;
import org.springframework.stereotype.Component;

/**
 * Argument parser for a {@link ObjectDescriptor.Filter} filtering on weapon damage.
 * @author Sarge
 */
@Component
public class WeaponDamageFilterArgumentParser implements ArgumentParser<ObjectDescriptor.Filter> {
	private final ArgumentParser<Damage.Type> parser;

	/**
	 * Constructor.
	 */
	public WeaponDamageFilterArgumentParser() {
		parser = new EnumArgumentParser<>("damage", Damage.Type.class);
	}

	// TODO
	@Override
	public Filter parse(WordCursor cursor) {
		// Parse literal
		final var weapons = new StringArgumentParser("filter.weapons");
		if(weapons.parse(cursor) == null) return null;

		// Parse literal
		final var that = new StringArgumentParser("filter.that");
		if(that.parse(cursor) == null) return null;

		// Parse damage-type
		final Damage.Type type = parser.parse(cursor);
		if(type == null) return null;

		// Create filter
		return descriptor -> {
			if(descriptor instanceof Weapon.Descriptor) {
				final Weapon.Descriptor weapon = (Weapon.Descriptor) descriptor;
				return weapon.damage().type() == type;
			}
			else {
				return false;
			}
		};
	}

	@Override
	public int count() {
		return 3;
	}
}
