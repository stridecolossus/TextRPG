package org.sarge.textrpg.loader;

import static java.util.stream.Collectors.toList;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TextNode;

/**
 * Loader for a {@link LootFactory}.
 * @author Sarge
 */
public class LootFactoryLoader {
	private final ValueLoader valueLoader = new ValueLoader();
	
	private final ObjectLoaderAdapter loader;
	
	public LootFactoryLoader(ObjectLoaderAdapter loader) {
		Check.notNull(loader);
		this.loader = loader;
	}

	/**
	 * Loads a loot-factory.
	 * @param node Text-node
	 * @return Loot-factory
	 */
	public LootFactory load(TextNode node) {
		final String type = node.name();
		switch(type) {
		case "money":
			final Value amount = valueLoader.load(node, "amount");
			return LootFactory.money(amount);
			
		case "compound":
			return LootFactory.compound(node.children().map(this::load).collect(toList()));
			
		case "chance":
			final Percentile chance = node.getAttribute("chance", null, Percentile.CONVERTER);
			final Value mod = valueLoader.load(node, "mod");
			final LootFactory delegate = load(node.child());
			return LootFactory.chance(chance, mod, delegate);
		
		default:
			// Load object
			final int num = node.getInteger("num", 1);
			final ObjectDescriptor descriptor = loader.loadDescriptor(node);
			if(!descriptor.isTransient()) throw node.exception("Cannot generate loot for a non-transient object: " + descriptor);
			return LootFactory.object(descriptor, num);
		}
	}
}
