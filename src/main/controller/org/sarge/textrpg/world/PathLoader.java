package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.util.Iterator;
import java.util.List;

import org.sarge.lib.xml.Element;
import org.springframework.stereotype.Service;

/**
 * Loader for a path.
 * @author Sarge
 */
@Service
public class PathLoader {
	private final LocationLoaderHelper helper;
	private final Location.Linker linker;

	/**
	 * Constructor.
	 * @param loader 	Helper
	 * @param linker	Location linker
	 */
	public PathLoader(LocationLoaderHelper loader, Location.Linker linker) {
		this.helper = notNull(loader);
		this.linker = notNull(linker);
	}

	/**
	 * Loads a path.
	 * @param xml		XML
	 * @param ctx		Context
	 * @return Start/end connectors
	 */
	public List<Location> load(Element xml, LoaderContext ctx) {
		// Load XML
		final Iterator<Element> itr = xml.children().iterator();
		if(!itr.hasNext()) throw xml.exception("Empty path");

		// Start builder
		final Route route = xml.attribute("route").optional(Route.CONVERTER).orElse(null);
		final Path path = new Path(ctx.area(), route);

		// Load path
		while(itr.hasNext()) {
			// Set direction
			final Element next = itr.next();
			next.attribute("dir").optional(Direction.CONVERTER).ifPresent(path::direction);

			// Load path location
			switch(next.name()) {
			case "location":
				// Load intermediate path location
				final Location.Descriptor descriptor = helper.loadDescriptor(next, ctx.terrain());
				path.add(descriptor);
				break;

			case "junction":
				// Load junction
				final String name = next.attribute("name").toText();
				final Location junction = ctx.junction(name);
				if(junction == null) throw xml.exception("Unknown junction: " + name);
				path.add(junction);
				break;

			default:
				throw xml.exception("Invalid path location type: " + next.name());
			}
		}

		// Register start/end connectors
		final List<Location> connectors = path.connectors();
		connectors.forEach(linker::add);
		return connectors;
	}
}
