package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ConverterAdapter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.Race.Builder;
import org.sarge.textrpg.loader.LoaderHelper;
import org.sarge.textrpg.loader.LootFactoryLoader;
import org.sarge.textrpg.loader.ObjectLoader;
import org.sarge.textrpg.loader.ObjectLoaderAdapter;
import org.sarge.textrpg.loader.SkillSetLoader;
import org.sarge.textrpg.object.DeploymentSlot;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Weapon;

/**
 * Loader for a {@link Race}.
 * @author Sarge
 */
public class RaceLoader {
	private final SkillSetLoader skillsLoader;
	private final ObjectLoaderAdapter objectLoader;
	private final LootFactoryLoader lootFactoryLoader;

	public RaceLoader(SkillSetLoader skillsLoader, ObjectLoaderAdapter objectLoader, LootFactoryLoader lootFactoryLoader) {
		Check.notNull(skillsLoader);
		Check.notNull(objectLoader);
		Check.notNull(lootFactoryLoader);
		this.skillsLoader = skillsLoader;
		this.objectLoader = objectLoader;
		this.lootFactoryLoader = lootFactoryLoader;
	}

	/**
	 * Loads a racial descriptor.
	 * @param xml XML
	 * @return Race
	 */
	public Race load(Element node) {
		// Start race
		final ConverterAdapter attrs = node.attributes();
		final Builder builder = new Builder(attrs.toString("name", null));

		// Load racial attributes
		builder
			.gender(attrs.toValue("gender", Gender.NEUTER, LoaderHelper.GENDER))
			.alignment(attrs.toValue("align", Alignment.NEUTRAL, LoaderHelper.ALIGNMENT))
			.size(attrs.toValue("size", Size.MEDIUM, Size.CONVERTER));

		// Load mount flag
		if(attrs.toBoolean("mount", false)) {
			builder.mount();
		}

		// Load default attribute values
		LoaderHelper.loadAttributes(node, builder.getAttributes());

		// Load weapon
		node.optionalChild("weapon").map(this::loadDefaultWeapon).ifPresent(builder::weapon);

		// Load default equipment
		final Collection<ObjectDescriptor> equipment = node.optionalChild("equipment").map(Element::children).orElse(Stream.empty()).map(objectLoader::loadDescriptor).collect(toList());
		final Set<DeploymentSlot> slots = new HashSet<>();
		for(final ObjectDescriptor descriptor : equipment) {
			final DeploymentSlot slot = descriptor.getEquipment().map(ObjectDescriptor.Equipment::getDeploymentSlot).orElseThrow(() -> node.exception("Cannot be equipped: " + descriptor));
			if(slots.contains(slot)) throw node.exception("Duplicate deployment slot: " + descriptor);
			builder.equipment(descriptor);
			slots.add(slot);
		}

		// Load default skills
		builder.skills(skillsLoader.load(node));

		// Load butcher loot-factory
		node.optionalChild("butcher").map(lootFactoryLoader::load).ifPresent(builder::butcherFactory);

		// Load corporeal flag
		if(!attrs.toBoolean("corporeal", true)) {
			builder.notCorporeal();
		}

		// Construct race
		return builder.build();
	}

	/**
	 * Loads the default weapon for this race.
	 */
	private Weapon loadDefaultWeapon(Element node) {
		final String name = node.attributes().toString("name", null);
		final ObjectDescriptor descriptor = new ObjectDescriptor(name);
		return ObjectLoader.loadWeaponDescriptor(node, descriptor, null);
	}
}
