package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.ConditionLoader;
import org.sarge.textrpg.common.EffectLoader;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.object.ObjectDescriptor.PassiveEffect;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.RegistryLoader;
import org.sarge.textrpg.util.ValueModifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Loader for object descriptors.
 * @author Sarge
 */
@Component("base.loader")
public class ObjectDescriptorLoader {
	private static final Converter<Cardinality> CARDINALITY = Converter.enumeration(Cardinality.class);

	/**
	 * Specifies the fixture policy for descriptors loaded by this loader.
	 */
	public enum Policy {
		/**
		 * Descriptors <b>must</b> be fixtures.
		 */
		FIXTURE,

		/**
		 * Descriptors must <b>not</b> be fixtures.
		 */
		OBJECT,

		/**
		 * Descriptors <b>can</b> be fixtures.
		 */
		ANY,
	}

	private final RegistryLoader<Material> material;
	private final ConditionLoader loader;

	/**
	 * Constructor.
	 * @param material 		Material registry/loader
	 * @param loader		Condition loader
	 */
	@Autowired
	public ObjectDescriptorLoader(RegistryLoader<Material> material, ConditionLoader loader) {
		this.material = notNull(material);
		this.loader = notNull(loader);
	}

	/**
	 * Loads an object descriptor.
	 * @param xml			XML
	 * @param policy		Fixture policy
	 * @return Object descriptor
	 */
	public ObjectDescriptor load(Element xml, Policy policy) {
		// Start descriptor
		final var builder = new ObjectDescriptor.Builder(xml.attribute("name").toText());

		// Load properties
		builder
			.weight(xml.attribute("weight").toInteger(0))
			.size(xml.attribute("size").toValue(Size.NONE, Size.CONVERTER))
			.alignment(xml.attribute("alignment").toValue(Alignment.NEUTRAL, Alignment.CONVERTER))
			.value(xml.attribute("value").toInteger(0));

		// Load periods
		builder
			.reset(xml.attribute("reset").toValue(Duration.ZERO, DurationConverter.CONVERTER))
			.decay(xml.attribute("decay").toValue(Duration.ZERO, DurationConverter.CONVERTER));

		// Check whether two-handed
		if(xml.attribute("two-handed").toBoolean(false)) {
			builder.twoHanded();
		}

		// Check whether fixture
		final boolean fixture = xml.attribute("fixture").toBoolean(policy == Policy.FIXTURE);
		if(fixture) {
			if(policy == Policy.OBJECT) throw xml.exception("Descriptor cannot be a fixture");
			builder.fixture();
		}
		else {
			if(policy == Policy.FIXTURE) throw xml.exception("Descriptor must be a fixture");
		}

		// Load characteristics
		builder
			.placement(xml.attribute("placement").toText(ObjectDescriptor.Characteristics.PLACEMENT_DEFAULT))
			.cardinality(xml.attribute("cardinality").toValue(Cardinality.SINGLE, CARDINALITY))
			.visibility(xml.attribute("vis").toValue(Percentile.ONE, Percentile.CONVERTER))
			.quiet(xml.attribute("quiet").toBoolean(false));

		// Load qualifier
		xml.attribute("qualifier").optional().ifPresent(builder::qualifier);

		// Load categories
		xml.children("category").map(Element::text).forEach(builder::category);

		// Load material
		xml.find("material").map(material::load).ifPresent(builder::material);

		// Load equipment descriptor
		builder
			.slot(xml.attribute("slot").toValue(Slot.NONE, Slot.CONVERTER))
			.armour(xml.attribute("armour").toInteger(0))
			.warmth(xml.attribute("warmth").toInteger(0))
			.noise(xml.attribute("noise").toValue(Percentile.ZERO, Percentile.CONVERTER));

		// Load conditions
		xml.find("conditions").stream().flatMap(Element::children).map(loader::load).forEach(builder::condition);

		// Load passive effects
		xml.find("passive").map(this::loadPassive).ifPresent(builder::passive);

		// Construct descriptor
		return builder.build();
	}

	/**
	 * Loads a passive effect.
	 * @param xml XML
	 * @return Passive effect
	 */
	private PassiveEffect loadPassive(Element xml) {
		final ValueModifier.Key modifier = EffectLoader.loadModifier(xml);
		final int size = xml.attribute("size").toInteger();
		return new PassiveEffect(modifier, size);
	}
}
