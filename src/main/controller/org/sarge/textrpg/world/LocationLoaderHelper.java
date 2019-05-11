package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.Converter;
import org.sarge.lib.xml.Element;
import org.springframework.stereotype.Component;

/**
 * Loader for a location descriptor.
 * @author Sarge
 */
@Component
public class LocationLoaderHelper {
	private static final Converter<LinkedExit.ReversePolicy> POLICY = Converter.enumeration(LinkedExit.ReversePolicy.class);

	private final LinkLoader loader;
	private final Location.Linker linker;

	/**
	 * Constructor.
	 * @param loader		Link loader
	 * @param linker		Location linker
	 */
	public LocationLoaderHelper(LinkLoader loader, Location.Linker linker) {
		this.loader = notNull(loader);
		this.linker = notNull(linker);
	}

	/**
	 * Loads a location descriptor.
	 * @param xml XML
	 * @param def Default terrain
	 * @return Descriptor
	 */
	public Location.Descriptor loadDescriptor(Element xml, Terrain def) {
		// Init descriptor
		final String name = xml.attribute("name").toText();
		final Terrain terrain = xml.attribute("terrain").toValue(def, Terrain.CONVERTER);
		final var builder = new Location.Descriptor.Builder().name(name).terrain(terrain);

		// Load properties
		xml.children("property").map(Element::text).map(Property.CONVERTER).forEach(builder::property);

		// Build descriptor
		return builder.build();
	}

	public final Location.Descriptor loadDescriptor(Element xml) {
		return loadDescriptor(xml, null);
	}

	/**
	 * Loads an exit.
	 * @param xml 			XML
	 * @param connector		Connector
	 * @param ctx			Context
	 */
	public void loadExit(Element xml, Location connector, LoaderContext ctx) {
		// Load exit properties
		final Direction dir = xml.attribute("dir").toValue(Direction.CONVERTER);
		final String dest = xml.attribute("dest").toText();

		// Load link descriptor
		final Link link = loader.load(xml, connector, ctx);

		// Load reverse exit properties
		final LinkedExit.ReversePolicy policy = xml.attribute("reverse").toValue(LinkedExit.ReversePolicy.INVERSE, POLICY);
		final Direction reverse = loadReverse(xml, policy);

		// Register exit
		final LinkedExit exit = new LinkedExit(connector, dir, link, dest, policy, reverse);
		linker.add(exit);
	}

	/**
	 * Loads the reverse direction.
	 * @param xml			XML
	 * @param policy		Reverse policy
	 * @return Reverse direction
	 */
	private static Direction loadReverse(Element xml, LinkedExit.ReversePolicy policy) {
		switch(policy) {
		case INVERSE:
		case SIMPLE:
			return xml.attribute("reverse-dir").optional(Direction.CONVERTER).orElse(null);

		default:
			return null;
		}
	}
}
