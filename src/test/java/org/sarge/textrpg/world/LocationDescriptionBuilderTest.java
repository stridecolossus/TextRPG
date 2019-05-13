package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.DefaultNameStore;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.Percentile;

public class LocationDescriptionBuilderTest {
	private LocationDescriptionBuilder builder;
	private SnowModel model;
	private Entity actor;
	private Location loc;
	private ArgumentFormatter.Registry formatters;

	@BeforeEach
	public void before() {
		// Create actors location
		loc = mock(Location.class);
		when(loc.name()).thenReturn("name");
		when(loc.exits()).thenReturn(ExitMap.EMPTY);
		when(loc.terrain()).thenReturn(Terrain.GRASSLAND);
		when(loc.contents()).thenReturn(new Contents());

		// Create area
		final Area area = new Area.Builder("area").build();
		when(loc.area()).thenReturn(area);
		model = mock(SnowModel.class);

		// Create actor
		actor = mock(Entity.class);
		when(actor.location()).thenReturn(loc);

		// Init formatters
		formatters = mock(ArgumentFormatter.Registry.class);

		// Create location description builder
		final NameStore store = new DefaultNameStore(Map.of("direction.east", "East"));
		builder = new LocationDescriptionBuilder(model, store, formatters);
	}

	@Test
	public void build() {
		final Description header = new Description.Builder("location.description.header").name("name").add("area", "area").build();
		final Description description = new Description("name.description");
		final Description empty = new Description("location.description.exits.none");
		assertEquals(List.of(header, description, empty), builder.build(actor, false));
	}

	@Test
	public void describeExit() {
		final Exit exit = Exit.of(Direction.EAST, loc);
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		final Description expected = new Description.Builder("location.description.exits").add("exits", "East", ArgumentFormatter.PLAIN).build();
		assertEquals(expected, builder.build(actor, false).get(2));
	}

	@Test
	public void describeContents() {
		final WorldObject obj = ObjectDescriptor.of("object").create();
		obj.parent(loc);
		when(actor.perceives(obj)).thenReturn(true);
		assertEquals(obj.describe(formatters), builder.build(actor, false).get(2));
	}

	@Test
	public void describeSnowLevel() {
		when(formatters.get("snow.level")).thenReturn(ArgumentFormatter.PLAIN);
		final Description expected = new Description.Builder("location.snow").add("level", Percentile.HALF, ArgumentFormatter.PLAIN).build();
		when(model.snow(loc)).thenReturn(50);
		assertEquals(expected, builder.build(actor, false).get(2));
	}
}
