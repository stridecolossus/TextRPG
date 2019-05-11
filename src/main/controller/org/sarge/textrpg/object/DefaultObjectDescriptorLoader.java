package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.Registry;
import org.springframework.stereotype.Component;

/**
 * Adapter that enforces fixture policy.
 * @author Sarge
 */
@Component
public class DefaultObjectDescriptorLoader implements Registry<ObjectDescriptor> {
	private final Registry<ObjectDescriptor> registry;
	private final ArchetypeLoader loader;

	/**
	 * Constructor.
	 * @param registry
	 */
	public DefaultObjectDescriptorLoader(Registry<ObjectDescriptor> registry, ArchetypeLoader loader) {
		this.loader = notNull(loader);
		this.registry = notNull(registry);
	}

	@Override
	public ObjectDescriptor get(String name) {
		return registry.get(name);
	}

	public ObjectDescriptor load(Element xml, ObjectDescriptorLoader.Policy policy) {
		final var attr = xml.attribute("descriptor");
		if(attr.isPresent()) {
			final String name = attr.toText();
			final ObjectDescriptor descriptor = registry.get(name);
			if(descriptor == null) throw xml.exception("Unknown registry entry: " + name);
			if(!isValid(descriptor, policy)) throw xml.exception(String.format("Invalid descriptor for policy: policy=%s descriptor=%s", policy, descriptor));
			return descriptor;
		}
		else {
			return loader.load(xml, policy);
		}
	}

	private static boolean isValid(ObjectDescriptor descriptor, ObjectDescriptorLoader.Policy policy) {
		switch(policy) {
		case FIXTURE:		return descriptor.isFixture();
		case OBJECT:		return !descriptor.isFixture();
		default:			return true;
		}
	}
}
