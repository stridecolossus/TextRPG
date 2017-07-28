package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.util.TextNode;

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
	public Value load(TextNode node, String name) {
		return node
			.optionalChild(name)
			.map(ValueLoader::load)
			.orElseGet(() -> Value.literal(node.getInteger(name, null)));
	}

	/**
	 * Loads a duration.
	 * @param node Text-node
	 * @param name Child name
	 * @return Duration value
	 */
	public Value loadDuration(TextNode node, String name) {
		return node
			.optionalChild(name)
			.map(ValueLoader::load)
			.orElseGet(() -> Value.literal((int) node.getAttribute(name, null, Converter.DURATION).toMillis()));
	}

	/**
	 * Loads a value.
	 * @param node Text-node
	 * @return Value
	 */
	public static Value load(TextNode node) {
		switch(node.name()) {
		case "literal":
			return Value.literal(node.getInteger("value", null));
			
		case "compound":
			final Value.Operator op = node.getAttribute("op", Value.Operator.ADD, OPERATOR_CONVERTER);
			final List<Value> values = node.children().map(ValueLoader::load).collect(toList());
			return Value.compound(op, values);
			
		case "random":
			return Value.random(node.getInteger("range", null));
			
		default:
			throw node.exception("Unknown value type: " + node.name());
		}
	}
}
