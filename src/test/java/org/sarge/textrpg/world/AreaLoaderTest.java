package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.Element.ElementException;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.object.LootFactoryLoader;
import org.sarge.textrpg.util.NameStore;

public class AreaLoaderTest {
	private AreaLoader loader;
	private WeatherController controller;

	@BeforeEach
	public void before() {
		// Create resource loader
		final LootFactoryLoader loot = mock(LootFactoryLoader.class);
		when(loot.load(any())).thenReturn(LootFactory.EMPTY);

		// Create weather controller
		controller = mock(WeatherController.class);

		// Load area
		loader = new AreaLoader(loot, controller);
	}

	@Test
	public void load() {
		// Build area XML
		final Element xml = new Element.Builder("area").attribute("name", "area")
			// Default location properties
			.child("property")
				.text("water")
				.end()
			// Add a view
			.child("view")
				.attribute("dir", "e")
				.attribute("view", "view")
				.end()
			// Add ambient event
			.child("ambient")
				.attribute("name", "ambient")
				.attribute("period", "1m")
				.attribute("repeat", "false")
				.end()
			// Add a resource
			.child("resource")
				.attribute("res", "res")
				.add(Element.of("loot-factory"))
				.end()
			// Add weather
			.child("weather")
				.child("min")
					.attribute("type", "temperature")
					.attribute("level", "1")
					.end()
				.child("max")
					.attribute("type", "temperature")
					.attribute("level", "1")
					.end()
				.end()
			.build();

		// Load area
		final Area area = loader.load(xml, Area.ROOT, NameStore.EMPTY);
		assertNotNull(area);

		// Check area
		assertEquals("area", area.name());
		assertEquals(Area.ROOT, area.parent());
		assertEquals(true, area.isProperty(Property.WATER));
		assertEquals(NameStore.EMPTY, area.store());

		// Check view
		assertNotNull(area.view(Direction.EAST));
		assertTrue(area.view(Direction.EAST).isPresent());

		// Check resources
		assertEquals(Optional.of(LootFactory.EMPTY), area.resource("res"));

		// Check ambient event
		final AmbientEvent ambient = new AmbientEvent("ambient", Duration.ofMinutes(1), false);
		assertEquals(Optional.of(ambient), area.ambient());

		// Check weather model
		final Weather weather = area.weather();
		assertNotNull(weather);
		verify(controller).add(weather);
	}

	@Test
	public void loadWeatherNone() {
		final Element xml = new Element.Builder("area").attribute("name", "area").attribute("weather", "none").build();
		final Area area = loader.load(xml, Area.ROOT, NameStore.EMPTY);
		assertEquals(Weather.NONE, area.weather());
	}

	@Test
	public void loadWeatherInvalidNone() {
		final Element xml = new Element.Builder("area").attribute("name", "area").attribute("weather", "cobblers").build();
		assertThrows(ElementException.class, () -> loader.load(xml, Area.ROOT, NameStore.EMPTY));
	}
}
