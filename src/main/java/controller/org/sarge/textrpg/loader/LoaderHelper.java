package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toSet;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	 * @param str Duration as a string
	 * @return Duration
	 */
	public static Duration parseDuration(String str) {
	    /**
	     * 99h99m99s
	     */


	    final Pattern pattern = Pattern.compile("(\\d{1,2}h)?(\\d{1,2}m)?(\\d{1,2}s)?");
	    final Matcher m = pattern.matcher(str);

//	    Duration.ofSeconds(Arrays.stream(runtime.split(":"))
//                .mapToInt(n -> Integer.parseInt(n))
//                .reduce(0, (n, m) -> n * 60 + m));

	    while(m.find()) {
	        System.out.println(m.start()+" "+m.end()+" "+m.group());
	    }

//	    return Duration.parse(str);
	    return null;
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
