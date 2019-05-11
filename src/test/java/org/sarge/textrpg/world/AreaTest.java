package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.View;
import org.sarge.textrpg.object.LootFactory;
import org.sarge.textrpg.util.NameStore;

public class AreaTest {
	private Area area;
	private LootFactory factory;
	private AmbientEvent ambient;
	private Weather weather;
	private NameStore store;

	@BeforeEach
	public void before() {
		factory = mock(LootFactory.class);
		weather = mock(Weather.class);
		ambient = new AmbientEvent("ambient", Duration.ZERO, true);
		store = mock(NameStore.class);
		area = new Area.Builder("area")
			.parent(Area.ROOT)
			.resource("res", factory)
			.weather(weather)
			.ambient(ambient)
			.view(Direction.EAST, View.of("view"))
			.store(store)
			.build();
	}

	@Test
	public void constructor() {
		assertEquals("area", area.name());
		assertEquals(Area.ROOT, area.parent());
		assertEquals(Optional.of(factory), area.resource("res"));
		assertEquals(Optional.of(ambient), area.ambient());
		assertEquals(weather, area.weather());
		assertNotNull(area.store());
	}

	@Test
	public void view() {
		final var view = area.view(Direction.EAST);
		assertNotNull(view);
		assertTrue(view.isPresent());
		assertEquals("view", view.get().describe(null));
	}

	@Test
	public void resourceAncestor() {
		final Area child = new Area.Builder("child").parent(area).resource("other", factory).build();
		assertEquals(Optional.of(factory), child.resource("other"));
		assertEquals(Optional.of(factory), child.resource("res"));
		assertEquals(Optional.empty(), child.resource("cobblers"));
	}

	@Test
	public void weatherAncestor() {
		final Area child = new Area.Builder("child").parent(area).weather(Weather.NONE).build();
		assertEquals(Weather.NONE, child.weather());
	}

	@Test
	public void constructorInvalidProperty() {
		assertThrows(IllegalArgumentException.class, () -> new Area.Builder("area").property(Property.FLOORLESS).build());
	}
}
