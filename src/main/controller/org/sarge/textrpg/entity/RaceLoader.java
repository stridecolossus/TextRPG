package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.entity.Race.Behaviour;
import org.sarge.textrpg.object.DefaultObjectDescriptorLoader;
import org.sarge.textrpg.object.LootFactoryLoader;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptorLoader;
import org.sarge.textrpg.object.Weapon;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.LoaderHelper;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.world.MovementManager;
import org.springframework.stereotype.Service;

/**
 * Loader for racial descriptors.
 * @author Sarge
 */
@Service
public class RaceLoader {
	private static final Converter<Gender> GENDER = Converter.enumeration(Gender.class);
	private static final Converter<Behaviour.Flag> BEHAVIOUR = Converter.enumeration(Behaviour.Flag.class);

	private final DefaultObjectDescriptorLoader loader;
	private final LootFactoryLoader loot;
	private final Registry<Skill> skills;

	/**
	 * Constructor.
	 * @param adapter		Object-descriptor loader
	 * @param loot			Loot-factory loader
	 * @param skills		Skills registry
	 */
	public RaceLoader(DefaultObjectDescriptorLoader loader, LootFactoryLoader loot, Registry<Skill> skills) {
		this.loader = notNull(loader);
		this.loot = notNull(loot);
		this.skills = notNull(skills);
	}

	/**
	 * Load race.
	 */
	public Race load(Element xml) {
		// Start race
		final var builder = new Race.Builder(xml.attribute("name").toText());

		// Load characteristics
		final Size size = xml.attribute("size").toValue(Size.NONE, Size.CONVERTER);
		builder
			.gender(xml.attribute("gender").toValue(Gender.NEUTER, GENDER))
			.alignment(xml.attribute("alignment").toValue(Alignment.NEUTRAL, Alignment.CONVERTER))
			.size(size)
			.weight(xml.attribute("weight").toInteger(0));

		// TODO - weight calculated from size unless over-ridden?

		// Load attributes
		xml.children("attribute").forEach(e -> loadAttribute(e, builder));

		// Load body parts
		xml.children("body").map(Element::text).forEach(builder::body);

		// Load categories
		xml.children("category").map(Element::text).forEach(builder::category);

		// Load default weapon
		xml.find("weapon").map(this::loadWeapon).ifPresent(builder::weapon);

		// Load equipment
		xml.children("equipment").map(e -> loader.load(e, ObjectDescriptorLoader.Policy.OBJECT)).forEach(builder::equipment);
		builder.noise(xml.attribute("noise").toValue(Percentile.ZERO, Percentile.CONVERTER));
		builder.tracks(xml.attribute("tracks").toValue(Percentile.ONE, Percentile.CONVERTER));

		// Load skills
		xml.attribute("vocation").optional().ifPresent(builder::vocation);
		xml.attribute("language").optional().map(skills::get).ifPresent(builder::language);
		xml.children("skill").map(Element::text).map(skills::get).forEach(builder::skill);
		// TODO - factions

		// Load behaviour
		builder.aggression(xml.attribute("aggression").toValue(Percentile.ZERO, Percentile.CONVERTER));
		builder.autoflee(xml.attribute("autoflee").toValue(Percentile.ZERO, Percentile.CONVERTER));
		LoaderHelper.enumeration(xml, "behaviour", BEHAVIOUR).forEach(builder::behaviour);

		// Load movement manager
		xml.find("movement").map(MovementManager::load).ifPresent(builder::movement);
		xml.attribute("period").optional().map(DurationConverter.CONVERTER).ifPresent(builder::period);

		// Load corpse descriptor
		if(size != Size.NONE) {
			builder.corpse();
		}
		xml.find("butcher").map(loot::load).ifPresent(builder::butcher);

		// Construct race
		return builder.build();
	}

	/**
	 * Loads an attribute.
	 */
	private static void loadAttribute(Element xml, Race.Builder builder) {
		final Attribute key = xml.attribute("attr").toValue(Attribute.CONVERTER);
		final int value = xml.attribute("value").toInteger();
		builder.attribute(key, value);
	}

	/**
	 * Loads default weapon for a race.
	 */
	private Weapon loadWeapon(Element xml) {
		// TODO - should load weapon explicitly
		final ObjectDescriptor descriptor = loader.load(xml, ObjectDescriptorLoader.Policy.OBJECT);
		if(descriptor instanceof Weapon.Descriptor) {
			final var weapon = (Weapon.Descriptor) descriptor;
			return weapon.create();
		}
		else {
			throw xml.exception("Not a weapon");
		}
	}
}
