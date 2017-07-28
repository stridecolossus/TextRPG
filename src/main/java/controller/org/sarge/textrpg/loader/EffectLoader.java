package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;

import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.entity.EffectMethod;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.util.TextNode;

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
	public Effect.Descriptor load(TextNode node) {
		final String name = node.getString("name", null);
		return new Effect.Descriptor(name, node.children().map(this::loadEffect).collect(toList()));
	}

	/**
	 * Loads an effect entry.
	 * @param node Text-node
	 * @return Effect
	 */
	private Effect loadEffect(TextNode node) {
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
	private static EffectMethod loadMethod(TextNode node) {
		switch(node.name()) {
		case "attribute":	return EffectMethod.attribute(node.getAttribute("attribute", null, Attribute.CONVERTER));
		case "value":		return EffectMethod.value(node.getAttribute("value", null, EntityValue.CONVERTER));
		default:			throw node.exception("Unknown effect method: " + node.name());
		}
	}
}
