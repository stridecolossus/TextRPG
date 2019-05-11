package org.sarge.textrpg.common;

import java.time.Duration;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.CalculationLoader;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.EntityValue.Key;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.ValueModifier;
import org.springframework.stereotype.Service;

/**
 * Loader for an effect.
 * @author Sarge
 */
@Service
public class EffectLoader {
	/**
	 * Effect type converter.
	 */
	public static final Converter<Effect.Group> GROUP = Converter.enumeration(Effect.Group.class);

	private final CalculationLoader loader = new CalculationLoader();

	/**
	 * Loads an effect descriptor.
	 * @param xml XML
	 * @return effect
	 */
	public Effect load(Element xml) {
		final String name = xml.attribute("name").toText();
		final ValueModifier.Key modifier = loadModifier(xml);
		final Calculation size = loader.load(xml.child("size"));
		final Effect.Group group = xml.attribute("group").toValue(Effect.Group.DEFAULT, GROUP);
		final Duration duration = xml.attribute("duration").toValue(DurationConverter.CONVERTER);
		final int times = xml.attribute("repeat").toInteger(1);
		return new Effect(name, modifier, size, group, duration, times);
	}

	/**
	 * Loads an effect modifier.
	 * @param xml XML
	 * @return Modifier
	 */
	public static ValueModifier.Key loadModifier(Element xml) {
		switch(xml.name()) {
		case "value":
			final Key.Type type = xml.attribute("type").toValue(EntityValue.Key.Type.CONVERTER);
			final EntityValue value = xml.attribute("value").toValue(EntityValue.CONVERTER);
			return value.key(type);

		case "attribute":
			return xml.attribute("attr").toValue(Attribute.CONVERTER);

		default:
			throw xml.exception("Invalid effect type: " + xml.name());
		}
	}

	/**
	 * Loads a damage descriptor.
	 * @param xml XML
	 * @return Damage descriptor
	 */
	public Damage loadDamage(Element xml) {
		final Damage.Type type = xml.attribute("type").toValue(Damage.Type.CONVERTER);
		final Calculation amount = loader.load(xml);
		final Effect effect = xml.find().map(this::load).orElse(Effect.NONE);
		return new Damage(type, amount, effect);
	}
}
