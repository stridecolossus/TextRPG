package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.ToggleListener;
import org.sarge.textrpg.entity.Alignment;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.Gender;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.MutableIntegerMap;
import org.sarge.textrpg.util.TextNode;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;

/**
 * Loader utilities.
 * @author Sarge
 */
public class LoaderHelper {
	public static final Converter<Terrain> TERRAIN = Converter.enumeration(Terrain.class);
	public static final Converter<Route> ROUTE = Converter.enumeration(Route.class);
	public static final Converter<Gender> GENDER = Converter.enumeration(Gender.class);
	public static final Converter<Alignment> ALIGNMENT = Converter.enumeration(Alignment.class);
	public static final Converter<Stance> STANCE = Converter.enumeration(Stance.class);

	private LoaderHelper() {
		// Utility class
	}
	
	/**
	 * Helper - Loads an enumeration set from the children of the given element.
	 * @param node			Text-node
	 * @param name			Child element name
	 * @param converter		Enumeration converter
	 * @return Set of enumeration constants
	 */
	public static <E extends Enum<E>> Set<E> loadEnumeration(TextNode node, String name, Converter<E> converter) {
		return node
			.optionalChild(name)
			.map(TextNode::children)
			.orElse(Stream.empty())
			.map(TextNode::name)
			.map(converter::convert)
			.collect(toSet());
	}
	
	/**
	 * The attribute-map has the following structure:
	 * <pre>
	 *  attributes {
	 * 	  attribute value
	 *    ...
	 *  }
	 * </pre>
	 * @see Attribute#CONVERTER
	 * @param node		Text-node
	 * @param attrs		Attributes map
	 * Loads a set of attributes.
	 */
	public static void loadAttributes(TextNode node, MutableIntegerMap<Attribute> attrs) {
		node.children().forEach(entry -> {
			final Attribute attr = Attribute.CONVERTER.convert(entry.name());
			final int value = Converter.INTEGER.convert(entry.value());
			attrs.set(attr, value);
		});
	}

	/**
	 * Loads a toggle listener.
	 * <p>
	 * The list of hours must be:
	 * <ul>
	 * <li>an even number of entries</li>
	 * <li>two-or-more entries</li>
	 * <li>range 0..23</li>
	 * <li>in ascending order</li>
	 * </ul>
	 * @param node		Text-node
	 * @param toggle	Toggle
	 * @return Listener
	 */
	public static ToggleListener loadToggleListener(TextNode node, Consumer<Boolean> toggle) {
		// Load list of hours
		final String attr = node.getString("hours", "7, 19");
		if(attr == null) throw node.exception("Expected list of hours");

		try {
			// Parse hours
			final String[] tokens = attr.trim().split(",");
			final int[] hours = Arrays.stream(tokens).map(String::trim).mapToInt(Integer::parseInt).toArray();
			
			// Create toggle listener
			return new ToggleListener(toggle, hours);
		}
		catch(IllegalArgumentException e) {
			throw node.exception(e);
		}
	}
}
