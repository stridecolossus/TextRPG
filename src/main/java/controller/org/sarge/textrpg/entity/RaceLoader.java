package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
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
import org.sarge.textrpg.util.TextNode;

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
	public Race load(TextNode node) {
		// Start race
		final Builder builder = new Builder(node.getString("name", null));

		// Load racial attributes
		builder
			.gender(node.getAttribute("gender", Gender.NEUTER, LoaderHelper.GENDER))
			.alignment(node.getAttribute("align", Alignment.NEUTRAL, LoaderHelper.ALIGNMENT))
			.size(node.getAttribute("size", Size.MEDIUM, Size.CONVERTER));

		// Load mount flag
		if(node.getBoolean("mount", false)) {
			builder.mount();
		}

		// Load default attribute values
		LoaderHelper.loadAttributes(node, builder.getAttributes());

		// Load weapon
		node.optionalChild("weapon").map(this::loadDefaultWeapon).ifPresent(builder::weapon);

		// Load default equipment
		final Collection<ObjectDescriptor> equipment = node.optionalChild("equipment").map(TextNode::children).orElse(Stream.empty()).map(objectLoader::loadDescriptor).collect(toList());
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
		if(!node.getBoolean("corporeal", true)) {
			builder.notCorporeal();
		}

		// Construct race
		return builder.build();
	}

	/**
	 * Loads the default weapon for this race.
	 */
	private Weapon loadDefaultWeapon(TextNode node) {
		final String name = node.getString("name", null);
		final ObjectDescriptor descriptor = new ObjectDescriptor(name);
		return ObjectLoader.loadWeaponDescriptor(node, descriptor, null);
	}
}
