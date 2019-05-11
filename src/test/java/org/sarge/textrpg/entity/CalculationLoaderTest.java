package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.Calculation;
import org.sarge.textrpg.util.ValueModifier;

public class CalculationLoaderTest {
	private CalculationLoader loader;
	private ValueModifier.Source src;

	@BeforeEach
	public void before() {
		// Create modifier
		final ValueModifier mod = mock(ValueModifier.class);
		when(mod.get()).thenReturn(42);

		// Create source
		src = mock(ValueModifier.Source.class);
		when(src.modifier(any())).thenReturn(mod);

		// Create loader
		loader = new CalculationLoader();
	}

	@Test
	public void literalEmbedded() {
		final Element xml = new Element.Builder("xml").attribute("literal", 42).build();
		final Calculation value = loader.load(xml);
		assertNotNull(value);
		assertEquals(42, value.evaluate(null));
	}

	@Test
	public void literal() {
		final Element xml = new Element.Builder("xml").child("literal").attribute("value", 42).end().build();
		final Calculation value = loader.load(xml);
		assertNotNull(value);
		assertEquals(42, value.evaluate(null));
	}

	@Test
	public void random() {
		final Element xml = new Element.Builder("xml").child("random").attribute("base", 0).attribute("range", 1).end().build();
		final Calculation value = loader.load(xml);
		assertNotNull(value);
		assertEquals(0, value.evaluate(null));
	}

	@Test
	public void scaled() {
		final Element xml = new Element.Builder("xml")
			.child("scaled")
				.attribute("scale", 2)
				.child("literal")
					.attribute("value", 3)
					.end()
				.end()
			.build();
		final Calculation value = loader.load(xml);
		assertNotNull(value);
		assertEquals(2 * 3, value.evaluate(null));
	}

	@Test
	public void compound() {
		final Element xml = new Element.Builder("xml")
			.child("compound")
				.attribute("op", "multiply")
				.child("literal").attribute("value", 2).end()
				.child("literal").attribute("value", 3).end()
				.end()
			.build();
		final Calculation value = loader.load(xml);
		assertNotNull(value);
		assertEquals(2 * 3, value.evaluate(null));
	}

	@Test
	public void value() {
		final Element xml = new Element.Builder("xml")
			.child("value")
				.attribute("value", "thirst")
				.end()
			.build();
		final Calculation value = loader.load(xml);
		assertNotNull(value);
		assertEquals(42, value.evaluate(src));
	}

	@Test
	public void attribute() {
		final Element xml = new Element.Builder("xml")
			.child("attribute")
				.attribute("attribute", "agility")
				.end()
			.build();
		final Calculation value = loader.load(xml);
		assertNotNull(value);
		assertEquals(42, value.evaluate(src));
	}
}
