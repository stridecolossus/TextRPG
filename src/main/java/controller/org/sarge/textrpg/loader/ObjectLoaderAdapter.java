package org.sarge.textrpg.loader;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.lib.util.Check;
import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.ElementException;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Registry;

/**
 * Looks up or loads an object.
 * @author Sarge
 */
public class ObjectLoaderAdapter {
	private static final Logger LOG = Logger.getLogger(ObjectLoaderAdapter.class.getName());
	
	private final ObjectLoader loader;
	private final Registry<ObjectDescriptor> descriptors;
	private final Registry<WorldObject> objects;
	
	public ObjectLoaderAdapter(ObjectLoader loader, Registry<ObjectDescriptor> descriptors, Registry<WorldObject> objects) {
		Check.notNull(loader);
		Check.notNull(descriptors);
		this.loader = loader;
		this.descriptors = descriptors;
		this.objects = objects;
	}
	
	public ObjectLoader getObjectLoader() {
		return loader;
	}
	
	public Registry<ObjectDescriptor> getDescriptors() {
		return descriptors;
	}

	public ObjectDescriptor loadDescriptor(Element node) {
		return loadDescriptor(node, null);
	}
	
	
	
	
	/**
	 * @param node
	 * @param def
	 * @return
	 */
	public ObjectDescriptor loadDescriptor(Element node, String def) {
		String type = null;
		final Optional<Element> child = node.optionalChild();
		if(child.isPresent()) {
			
			if(child.get().name().equals("type")) {
				type = child.get().text();
			}
			else {
				// Load custom object descriptor
				final ObjectDescriptor descriptor = loader.load(child.get());
				if((def != null) && (descriptor.getClass() != ObjectDescriptor.class)) {
					throw node.exception("Expected basic object for a portal");
				}
				descriptors.add(descriptor);
				LOG.log(Level.FINE, "Custom descriptor: {0}", descriptor.getName());
				return descriptor;
			}
		}

		// Use default descriptor
		if(type == null) {
			if(def == null) {
				throw node.exception("Expected descriptor reference or definition");
			}
			else {
				type = def;
			}
		}
		
		// Otherwise lookup descriptor
		final ObjectDescriptor descriptor = descriptors.find(type);
		if(descriptor == null) throw node.exception("Unknown descriptor: " + type);
		return descriptor;
	}

	/**
	 * Loads an object for a location.
	 * @param node		Text node
	 * @param child		Whether to use the child element
	 * @return Object
	 * @throw ElementException if the object is not a fixture
	 * @see WorldObject#isFixture()
	 */
	public WorldObject loadObject(Element node, boolean child) {
		final String name = node.getValue("type");
		if(name == null) {
			// Load one-off custom descriptor
			final ObjectDescriptor descriptor;
			try {
				descriptor = loader.load(child ? node.child() : node);
			}
			catch(ElementException e) {
				throw e;
			}
			catch(Exception e) {
				throw node.exception(e);
			}
			final WorldObject obj = descriptor.create();
			if(!obj.isFixture()) {
				throw node.exception("Not a fixture: " + descriptor);
			}
			objects.add(obj);
			return obj;
		}
		else {
			// Lookup descriptor
			final ObjectDescriptor descriptor = descriptors.find(name);
			return descriptor.toFixture().create();
		}
	}
}
