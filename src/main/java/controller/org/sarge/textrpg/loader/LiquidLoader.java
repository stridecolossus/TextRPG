package org.sarge.textrpg.loader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.entity.Effect;
import org.sarge.textrpg.object.Liquid;
import org.sarge.textrpg.util.Registry;

/**
 * Loader for a {@link Liquid}.
 * @author Sarge
 */
public class LiquidLoader {
	private static final Logger LOG = Logger.getLogger(LiquidLoader.class.getName());
	
	private final EffectLoader effectLoader = new EffectLoader();
	
	private final Registry<Liquid> liquids = new Registry<>(Liquid::getName);
	
	/**
	 * Constructor.
	 */
	public LiquidLoader() {
		liquids.add(Liquid.WATER, null);
		liquids.add(Liquid.OIL, null);
	}
	
	/**
	 * Load pre-defined liquids.
	 */
	public void loadAll(Element node) {
		node.children().forEach(this::loadLiquid);
	}
	
	/**
	 * Loads or looks up a liquid.
	 * @param node Text-node
	 * @return Liquid descriptor
	 */
	public Liquid load(Element node) {
		final String name = node.name();
		final Liquid liquid = liquids.find(name);
		if(liquid == null) {
			return loadLiquid(node);
		}
		else {
			return liquid;
		}
	}
	
	private Liquid loadLiquid(Element node) {
		final int alcohol = node.attributes().toInteger("alcohol", 0);
		final Effect.Descriptor effect = node.optionalChild().map(effectLoader::load).orElse(Effect.NONE);
		final Liquid liquid = new Liquid(node.name(), alcohol, effect);
		liquids.add(liquid);
		LOG.log(Level.FINE, "Custom liquid: {0}", liquid.getName());
		return liquid;
	}
}
