package org.sarge.textrpg.loader;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.object.Cardinality;
import org.sarge.textrpg.object.DeploymentSlot;
import org.sarge.textrpg.object.Material;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.object.ObjectDescriptor.Equipment;
import org.sarge.textrpg.object.ObjectDescriptor.EquipmentBuilder;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextNode;

/**
 * Loader for object descriptors.
 * @author Sarge
 */
public class ObjectDescriptorLoader {
	private static final Converter<Cardinality> CARDINALITY = Converter.enumeration(Cardinality.class);
	private static final Converter<Size> SIZE = Converter.enumeration(Size.class);
	private static final Converter<Emission.Type> EMISSION = Converter.enumeration(Emission.Type.class);
	private static final Converter<DamageType> DAMAGE = Converter.enumeration(DamageType.class);
	private static final Converter<DeploymentSlot> SLOT = Converter.enumeration(DeploymentSlot.class);

	private static final Duration DEFAULT_DURATION = Duration.ofMinutes(2);

	private final EffectLoader effectLoader = new EffectLoader();
	private final ConditionLoader conditionLoader;

	private Converter<Integer> weightConverter = Converter.INTEGER;
	private Converter<Integer> valueConverter = Converter.INTEGER;
	private Converter<Integer> armourConverter = Converter.INTEGER;
	private Converter<Percentile> lightConverter = Percentile.CONVERTER;
	private Converter<Integer> strengthConverter = Converter.INTEGER;

	/**
	 * Constructor.
	 * @param effectLoader			Effect loader
	 */
	public ObjectDescriptorLoader(ConditionLoader conditionLoader) {
		Check.notNull(conditionLoader);
		this.conditionLoader = conditionLoader;
	}

	public void setWeightConverter(Converter<Integer> weightConverter) {
		this.weightConverter = weightConverter;
	}

	public void setValueConverter(Converter<Integer> valueConverter) {
		this.valueConverter = valueConverter;
	}

	public void setArmourConverter(Converter<Integer> armourConverter) {
		this.armourConverter = armourConverter;
	}

	public void setLightConverter(Converter<Percentile> lightConverter) {
		this.lightConverter = lightConverter;
	}

	public void setStrengthConverter(Converter<Integer> strengthConverter) {
		this.strengthConverter = strengthConverter;
	}

	/**
	 * Loads an object descriptor from the given XML.
	 * @param node Text-node
	 * @return Object descriptor
	 */
	public ObjectDescriptor load(TextNode node) {
		// Start descriptor
		final Builder builder = new Builder(node.getString("name", null));

		// Load physical properties
		builder.value(node.getAttribute("value", 0, valueConverter));
		builder.size(node.getAttribute("size", Size.NONE, SIZE));

		// Check for fixtures
		final boolean fixture = node.getBoolean("fixture", false);
		if(fixture) {
			builder.weight(ObjectDescriptor.IMMOVABLE);
		}
		else {
			builder.weight(node.getAttribute("weight", 0, weightConverter));
		}

		// Load visible characteristics
		builder
			.description(node.getString("description", ObjectDescriptor.DEFAULT_DESCRIPTION))
			.colour(node.getString("colour", ObjectDescriptor.COLOUR_NONE))
			.cardinality(node.getAttribute("cardinality", Cardinality.SINGLE, CARDINALITY))
			.visibility(node.getAttribute("vis", Percentile.ONE, Percentile.CONVERTER));

		// Check for quiet objects
		if(node.getBoolean("quiet", false)) {
			builder.quiet();
		}

		// Load categories
		final String cat = node.getValue("category");
		if(cat == null) {
			node.optionalChild("category").map(TextNode::children).orElse(Stream.empty()).map(TextNode::name).forEach(builder::category);
		}
		else {
			builder.category(cat);
		}

		// Load emissions
		node.children("emission").map(this::loadEmission).forEach(builder::emission);

		// Load material
		node.optionalChild("material").map(this::loadMaterial).ifPresent(builder::material);

		// Load equipment descriptor
		final String slot = node.getValue("slot");
		if(slot == null) {
			node.optionalChild("equipment").map(this::loadEquipment).ifPresent(builder::equipment);
		}
		else {
			final DeploymentSlot ds = SLOT.convert(slot);
			final Equipment equipment = new EquipmentBuilder().slot(ds).build();
			builder.equipment(equipment);
		}

		// Load reset period
		final Duration def;
		switch(node.name()) {
		case "portal":
		case "door":
		case "gate":
			def = DEFAULT_DURATION;
			break;

		default:
			def = Duration.ZERO;
			break;
		}
		builder.reset(node.getAttribute("reset", def, Converter.DURATION).toMillis());

		// Create descriptor
		return builder.build();
	}

	/**
	 * Loads an emission descriptor.
	 * @param node Text-node
	 * @return Emission
	 */
	private Emission loadEmission(TextNode node) {
		final Emission.Type type = node.getAttribute("type", null, EMISSION);
		final String name = type.hasName() ? node.getString("name", null) : null;
		final Percentile intensity = node.getAttribute("intensity", Percentile.ONE, lightConverter);
		return new Emission(name, type, intensity);
	}

	/**
	 * Loads a material descriptor.
	 * @param node Text-node
	 * @return Material
	 */
	private Material loadMaterial(TextNode node) {
		final String name = node.getString("name", null);
		final Set<Emission.Type> transparent = LoaderHelper.loadEnumeration(node, "transparent", EMISSION);
		final Set<DamageType> sustains = LoaderHelper.loadEnumeration(node, "sustains", DAMAGE);
		final int str = node.getAttribute("strength", 0, strengthConverter);
		return new Material(name, transparent, sustains, str);
	}

	/**
	 * Loads an equipment descriptor.
	 * @param node Text-node
	 * @param builder	Descriptor builder
	 */
	private Equipment loadEquipment(TextNode node) {
		return new EquipmentBuilder()
			.slot(node.getAttribute("slot", null, SLOT))
			.twoHanded(node.getAttribute("two-handed", false, Converter.BOOLEAN))
			.condition(node.optionalChild("condition").map(conditionLoader::load).orElse(Condition.TRUE))
			.armour(node.getAttribute("armour", 0, armourConverter))
			.passive(node.optionalChild("passive").map(effectLoader::load).orElse(Effect.NONE))
			.build();
	}
}
