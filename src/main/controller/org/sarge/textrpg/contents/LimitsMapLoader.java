package org.sarge.textrpg.contents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.Element.Attribute;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.LimitedContents.Limit;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.Slot;

/**
 * Loader for a {@link LimitsMap}.
 * @author Sarge
 */
public class LimitsMapLoader {
	/**
	 * Loads contents limits.
	 * @param xml XML
	 * @return Limits
	 */
	public LimitsMap load(Element xml) {
		final Map<String, Limit> limits = new HashMap<>();
		final Consumer<Element> loader = e -> {
			final String reason = e.attribute("reason").toText();
			final Limit limit = limit(e);
			limits.put(reason, limit);
		};
		xml.children().forEach(loader);
		return new LimitsMap(limits);
	}

	/**
	 * Loads a contents limit.
	 * @param xml XML
	 * @return Limit
	 */
	private static Limit limit(Element xml) {
		switch(xml.name()) {
		case "capacity":
			final int capacity = xml.attribute("capacity").toInteger();
			return Limit.capacity(capacity);

		case "size":
			final Size size = xml.attribute("size").toValue(Size.CONVERTER);
			return Limit.size(size);

		case "weight":
			final int weight = xml.attribute("weight").toInteger();
			return Limit.weight(weight);

		case "category":
			final Set<String> cats = new HashSet<>();
			final Attribute cat = xml.attribute("cat");
			if(cat.isPresent()) {
				cats.add(cat.toText());
			}
			else {
				xml.children("cat").map(Element::text).forEach(cats::add);
			}
			return Container.categoryLimit(cats);

		case "slot":
			final Slot slot = xml.attribute("slot").toValue(Slot.CONVERTER);
			return Container.slotLimit(slot);

		default:
			throw xml.exception("Invalid limit: " + xml.name());
		}
	}
}
