package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.entity.EffectMethod;
import org.sarge.textrpg.entity.EntityValue;

/**
 * Loader for an {@link Effect.Descriptor}.
 * @author Sarge
 */
public class EffectLoader {
	private final ValueLoader valueLoader = new ValueLoader();

	/**
	 * Loads an effect descriptor.
	 * @param node Text-node
	 * @return Effect
	 */
	public Effect.Descriptor load(Element node) {
		final String name = node.attributes().toString("name", null);
		return new Effect.Descriptor(name, node.children().map(this::loadEffect).collect(toList()));
	}

	/**
	 * Loads an effect entry.
	 * @param node Text-node
	 * @return Effect
	 */
	private Effect loadEffect(Element node) {
		final EffectMethod method = loadMethod(node);
		final Value size = valueLoader.load(node, "size");
		final Value duration = valueLoader.loadDuration(node, "duration");
		return new Effect(method, size, duration);
	}

	/**
	 * Loads an effect method.
	 * @param node Text-node
	 * @return Effect method
	 */
	private static EffectMethod loadMethod(Element node) {
		switch(node.name()) {
		case "attribute":	return EffectMethod.attribute(node.attributes().toValue("attribute", null, Attribute.CONVERTER));
		case "value":		return EffectMethod.value(node.attributes().toValue("value", null, EntityValue.CONVERTER));
		default:			throw node.exception("Unknown effect method: " + node.name());
		}
	}
}
