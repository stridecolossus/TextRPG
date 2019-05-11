package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;

public class EffectLoaderTest {
	private EffectLoader loader;

	@BeforeEach
	public void before() {
		loader = new EffectLoader();
	}

	@Test
	public void loadDamage() {
		// Build XML
		final Element xml = new Element.Builder("xml")
			.attribute("type", "cold")
			.attribute("literal", 42)
			.build();

		// Load damage
		final Damage damage = loader.loadDamage(xml);
		assertEquals(Damage.Type.COLD, damage.type());
		assertEquals(42, damage.amount().evaluate(null));
		assertEquals(Effect.NONE, damage.effect());
	}
}
