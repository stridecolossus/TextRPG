package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.entity.EntityValue.Key;
import org.sarge.textrpg.util.Calculation;

/**
 * Loader for a calculation.
 * @author Sarge
 */
public class CalculationLoader {
	private static final Converter<Calculation.Operator> OPERATION = Converter.enumeration(Calculation.Operator.class);
	private static final Converter<EntityValue.Key.Type> KEY = Converter.enumeration(EntityValue.Key.Type.class);

	/**
	 * Loads an integer-value.
	 * <p>
	 * This is either a literal integer-value specified by a <i>literal</i> attribute or by a child element:
	 * <p>
	 * <table border='1'>
	 * <tr><th>name</th><th>attribute(s)</th><th>description</th></tr>
	 * <tr><td><tt>literal</tt></td><td><tt>value</tt></td><td>integer literal</td></tr>
	 * <tr><td><tt>random</tt></td><td><tt>base, range</tt></td><td>randomised value</td></tr>
	 * <tr><td><tt>scaled</tt></td><td><tt>delegate</tt></td><td>scaled value</td></tr>
	 * <tr><td><tt>compound</tt></td><td><tt>op</tt></td><td>compound value</td></tr>
	 * <tr><td><tt>percentile</tt></td><td><tt>delegate</tt></td><td>value represented as an inverted percentile</td></tr>
	 * <tr><td><tt>value</tt></td><td><tt>entity-value</tt></td><td>entity-value</td></tr>
	 * <tr><td><tt>attribute</tt></td><td><tt>attribute</tt></td><td>attribute value</td></tr>
	 * </table>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>a <i>compound</i> value is assumed to have one-or-more child elements that are reduced by the given operator (see {@link IntegerValue.Operator)</li>
	 * </ul>
	 * <p>
	 * @param xml XML
	 * @return Integer-value
	 * @see Source
	 */
	public Calculation load(Element xml) {
		return xml.attribute("literal").optional(Converter.INTEGER).map(Calculation::literal).orElseGet(() -> loadLocal(xml.child()));
	}

	/**
	 * Loads an integer-value for an entity.
	 * @param xml XML
	 * @return Integer-value
	 */
	private Calculation loadLocal(Element xml) {
		switch(xml.name()) {
		case "literal":
			final int value = xml.attribute("value").toInteger();
			return Calculation.literal(value);

		case "random":
			final int base = xml.attribute("base").toInteger();
			final int range = xml.attribute("range").toInteger();
			return Calculation.random(base, range);

		case "scaled":
			final Calculation delegate = loadLocal(xml.child());
			final int scale = xml.attribute("scale").toInteger();
			return Calculation.scaled(delegate, scale);

		case "compound":
			final var values = xml.children().map(this::loadLocal).collect(toList());
			final Calculation.Operator op = xml.attribute("op").toValue(Calculation.Operator.SUM, OPERATION);
			return Calculation.compound(values, op);

		// TODO - is this general enough? or factor out to controller? certainly not used by initialiser
		case "percentile":
			return Calculation.percentile(loadLocal(xml.child()));

		case "value":
			final EntityValue entityValue = xml.attribute("value").toValue(EntityValue.CONVERTER);
			final EntityValue.Key.Type type = xml.attribute("type").toValue(Key.Type.DEFAULT, KEY);
			return src -> src.modifier(entityValue.key(type)).get();

		case "attribute":
			final Attribute attr = xml.attribute("attribute").toValue(Attribute.CONVERTER);
			return src -> src.modifier(attr).get();

		default:
			throw xml.exception("Invalid integer-value type: " + xml.name());
		}
	}
}
