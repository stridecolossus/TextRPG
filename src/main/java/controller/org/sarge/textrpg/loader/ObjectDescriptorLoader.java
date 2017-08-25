package org.sarge.textrpg.loader;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.ConverterAdapter;
import org.sarge.lib.xml.Element;
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
	public ObjectDescriptor load(Element node) {
		// Start descriptor
		final ConverterAdapter attrs = node.attributes();
		final Builder builder = new Builder(attrs.toString("name", null));

		// Load physical properties
		builder.value(attrs.toValue("value", 0, valueConverter));
		builder.size(attrs.toValue("size", Size.NONE, SIZE));

		// Check for fixtures
		final boolean fixture = attrs.toBoolean("fixture", false);
		if(fixture) {
			builder.weight(ObjectDescriptor.IMMOVABLE);
		}
		else {
			builder.weight(attrs.toValue("weight", 0, weightConverter));
		}

		// Load visible characteristics
		builder
			.description(attrs.toString("description", ObjectDescriptor.DEFAULT_DESCRIPTION))
			.colour(attrs.toString("colour", ObjectDescriptor.COLOUR_NONE))
			.cardinality(attrs.toValue("cardinality", Cardinality.SINGLE, CARDINALITY))
			.visibility(attrs.toValue("vis", Percentile.ONE, Percentile.CONVERTER));

		// Check for quiet objects
		if(attrs.toBoolean("quiet", false)) {
			builder.quiet();
		}

		// Load categories
		final Optional<String> cat = attrs.getOptional("category", Converter.STRING);
		if(cat.isPresent()) {
			builder.category(cat.get());
		}
		else {
			node.optionalChild("category")
				.map(Element::children)
				.orElse(Stream.empty())
				.map(Element::name)
				.forEach(builder::category);
		}

		// Load emissions
		node.children("emission").map(this::loadEmission).forEach(builder::emission);

		// Load material
		node.optionalChild("material").map(this::loadMaterial).ifPresent(builder::material);

		// Load equipment descriptor
		final Optional<DeploymentSlot> slot = attrs.getOptional("slot", SLOT);
		if(slot.isPresent()) {
			final Equipment equipment = new EquipmentBuilder().slot(slot.get()).build();
			builder.equipment(equipment);
		}
		else {
			node.optionalChild("equipment").map(this::loadEquipment).ifPresent(builder::equipment);
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
		builder.reset(attrs.toValue("reset", def, LoaderHelper::parseDuration).toMillis());

		// Create descriptor
		return builder.build();
	}

	/**
	 * Loads an emission descriptor.
	 * @param node Text-node
	 * @return Emission
	 */
	private Emission loadEmission(Element node) {
		final ConverterAdapter attrs = node.attributes();
		final Emission.Type type = attrs.toValue("type", null, EMISSION);
		final String name = type.hasName() ? attrs.toString("name", null) : null;
		final Percentile intensity = attrs.toValue("intensity", Percentile.ONE, lightConverter);
		return new Emission(name, type, intensity);
	}

	/**
	 * Loads a material descriptor.
	 * @param node Text-node
	 * @return Material
	 */
	private Material loadMaterial(Element node) {
		final ConverterAdapter attrs = node.attributes();
		final String name = attrs.toString("name", null);
		final Set<Emission.Type> transparent = LoaderHelper.loadEnumeration(node, "transparent", EMISSION);
		final Set<DamageType> sustains = LoaderHelper.loadEnumeration(node, "sustains", DAMAGE);
		final int str = attrs.toValue("strength", 0, strengthConverter);
		return new Material(name, transparent, sustains, str);
	}

	/**
	 * Loads an equipment descriptor.
	 * @param node Text-node
	 * @param builder	Descriptor builder
	 */
	private Equipment loadEquipment(Element node) {
		final ConverterAdapter attrs = node.attributes();
		return new EquipmentBuilder()
			.slot(attrs.toValue("slot", null, SLOT))
			.twoHanded(attrs.toValue("two-handed", false, Converter.BOOLEAN))
			.condition(node.optionalChild("condition").map(conditionLoader::load).orElse(Condition.TRUE))
			.armour(attrs.toValue("armour", 0, armourConverter))
			.passive(node.optionalChild("passive").map(effectLoader::load).orElse(Effect.NONE))
			.build();
	}
}
