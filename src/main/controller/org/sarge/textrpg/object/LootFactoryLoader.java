package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;

import org.sarge.lib.xml.Element;
import org.springframework.stereotype.Service;

/**
 * Loader for a loot-factory.
 * @author Sarge
 */
@Service
public class LootFactoryLoader {
	/**
	 * Loads a loot-factory.
	 * @param xml XML
	 * @return Loot-factory
	 */
	public LootFactory load(Element xml) {
		switch(xml.name()) {
		case "object":
			final int num = xml.attribute("count").toInteger(1);
			final ObjectDescriptor descriptor = null; // TODO
			return LootFactory.of(descriptor, num);

		case "compound":
			final var list = xml.children().map(this::load).collect(toList());
			return actor -> list.stream().flatMap(f -> f.generate(actor));

		case "money":
			final int amount = xml.attribute("amount").toInteger();
			return LootFactory.money(amount);

		default:
			throw xml.exception("Invalid loot-factory type: " + xml.name());
		}
	}
}
