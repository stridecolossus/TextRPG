package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toSet;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.ToggleListener;
import org.sarge.textrpg.entity.Alignment;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.Gender;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.MutableIntegerMap;
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
	 * Parses a duration.
	 * <br>
	 * The duration is a colon-separated string with the format <tt>dd:hh:mm:ss</tt> specifying the number of days, hours, minutes and seconds, with the first three being optional.
	 * <br>
	 * Examples:
	 * <ul>
	 * <li><tt>01:02:03</tt> - 1 hour, 2 minutes and 3 seconds.</li>
	 * <li><tt>4:5</tt> - 4 minutes and 5 seconds.</li>
	 * <li><tt>06</tt> - 6 seconds.</li>
	 * </ul>
	 * @param str Duration string
	 * @return Duration
	 */
	public static Duration parseDuration(String str) {
	    if(str.startsWith("P")) {
	        return Duration.parse(str);
	    }
	    else {
	        return Duration.ofSeconds(Arrays.stream(str.split(":"))
                .mapToInt(n -> Integer.parseInt(n))
                .reduce(0, (n, m) -> n * 60 + m));
	    }
	}

	/**
	 * Helper - Loads an enumeration set from the children of the given element.
	 * @param node			Text-node
	 * @param name			Child element name
	 * @param converter		Enumeration converter
	 * @return Set of enumeration constants
	 */
	public static <E extends Enum<E>> Set<E> loadEnumeration(Element node, String name, Converter<E> converter) {
		return node
			.optionalChild(name)
			.map(Element::children)
			.orElse(Stream.empty())
			.map(Element::name)
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
	public static void loadAttributes(Element node, MutableIntegerMap<Attribute> attrs) {
		node.children().forEach(entry -> {
			final Attribute attr = Attribute.CONVERTER.convert(entry.name());
			final int value = Converter.INTEGER.convert(entry.text());
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
	public static ToggleListener loadToggleListener(Element node, Consumer<Boolean> toggle) {
		// Load list of hours
		final String attr = node.attributes().toString("hours", "7, 19");
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
