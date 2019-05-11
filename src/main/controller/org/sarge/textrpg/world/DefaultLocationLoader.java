package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Consumer;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.object.ObjectLoader;
import org.sarge.textrpg.object.ShopLoader;
import org.springframework.stereotype.Service;

/**
 * Loader for a default location.
 * @author Sarge
 */
@Service
public class DefaultLocationLoader {
	private final LocationLoaderHelper loader;
	private final Location.Linker linker;
	private final ObjectLoader objectLoader;
	private final ShopLoader shopLoader;

	/**
	 * Constructor.
	 * @param loader 			Helper
	 * @param linker			Location linker
	 * @param ojectLoader		Contents loader
	 * @param shopLoader		Shop loader
	 */
	public DefaultLocationLoader(LocationLoaderHelper loader, Location.Linker linker, ObjectLoader objectLoader, ShopLoader shopLoader) {
		this.loader = notNull(loader);
		this.linker = notNull(linker);
		this.objectLoader = notNull(objectLoader);
		this.shopLoader = notNull(shopLoader);
	}

	/**
	 * Loads a world location.
	 * @param xml 		XML
	 * @param ctx		Context
	 * @return Location
	 */
	public DefaultLocation load(Element xml, LoaderContext ctx) {
		// Load location
		final Location.Descriptor descriptor = loader.loadDescriptor(xml, ctx.terrain());
		final DefaultLocation.Builder builder = new DefaultLocation.Builder().descriptor(descriptor).area(ctx.area());

		// Load orphan flag
		if(xml.attribute("orphan").toBoolean(false)) {
			builder.orphan();
		}

		// Build location
		final DefaultLocation loc = builder.build();

		// Register location as a connector
		linker.add(loc);

		// Load simple exits
		final Consumer<Element> linksLoader = e -> loader.loadExit(e, loc, ctx);
		xml.children("link").forEach(linksLoader);
		xml.children("route").forEach(linksLoader);

		// Load custom exits
		xml.find("links").stream().flatMap(Element::children).forEach(linksLoader);

		// Load contents
		// TODO - move to helper?
		xml.find("contents").stream().flatMap(Element::children).map(e -> objectLoader.load(e, ctx)).forEach(obj -> obj.parent(loc));

		// Load shop
		xml.find("shop").ifPresent(e -> shopLoader.load(e, loc, ctx));

		return loc;
	}
}
