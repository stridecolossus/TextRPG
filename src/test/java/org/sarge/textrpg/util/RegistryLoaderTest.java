package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;

public class RegistryLoaderTest {
	@Test
	public void load() {
		// Create a registry with a pre-defined entry
		final Element predefined = Element.of("predefined");
		final Registry<Element> registry = new Registry.Builder<>(Element::name).add(predefined).build();

		// Create loader
		final RegistryLoader<Element> adapter = new RegistryLoader<>(registry, Function.identity(), "name");

		// Load a custom entry
		final Element custom = Element.of("custom");
		assertEquals(custom, adapter.load(custom));

		// Load the pre-defined entry
		final Element xml = new Element.Builder("xml").attribute("name", "predefined").build();
		assertEquals(predefined, adapter.load(xml));
	}
}
