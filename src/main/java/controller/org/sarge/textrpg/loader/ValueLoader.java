package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Value;

/**
 * Loader for a {@link Value}.
 * @author Sarge
 */
public class ValueLoader {
	private static final Converter<Value.Operator> OPERATOR_CONVERTER = Converter.enumeration(Value.Operator.class);

	/**
	 * Loads a value from the given child element or literal.
	 * @param node Text-node
	 * @param name		Value name
	 * @return Value
	 */
	public Value load(Element node, String name) {
		return node
			.optionalChild(name)
			.map(ValueLoader::load)
			.orElseGet(() -> Value.literal(node.attributes().toInteger(name, null)));
	}

	/**
	 * Loads a duration.
	 * @param node Text-node
	 * @param name Child name
	 * @return Duration value
	 */
	public Value loadDuration(Element node, String name) {
		return node
			.optionalChild(name)
			.map(ValueLoader::load)
			.orElseGet(() -> Value.literal((int) node.attributes().toValue(name, null, Converter.DURATION).toMillis()));
	}

	/**
	 * Loads a value.
	 * @param node Text-node
	 * @return Value
	 */
	public static Value load(Element node) {
		switch(node.name()) {
		case "literal":
			return Value.literal(node.attributes().toInteger("value", null));
			
		case "compound":
			final Value.Operator op = node.attributes().toValue("op", Value.Operator.ADD, OPERATOR_CONVERTER);
			final List<Value> values = node.children().map(ValueLoader::load).collect(toList());
			return Value.compound(op, values);
			
		case "random":
			return Value.random(node.attributes().toInteger("range", null));
			
		default:
			throw node.exception("Unknown value type: " + node.name());
		}
	}
}
