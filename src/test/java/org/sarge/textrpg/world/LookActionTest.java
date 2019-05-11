package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.View;
import org.sarge.textrpg.entity.EmissionNotification;
import org.sarge.textrpg.entity.PerceptionCalculator;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Window;
import org.sarge.textrpg.util.Clock;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.LookAction.AroundArgument;

public class LookActionTest extends ActionTestBase {
	private LookAction action;
	private Clock clock;
	private LightLevelProvider light;
	private EmissionController controller;
	private PerceptionCalculator perception;

	@BeforeEach
	public void before() {
		// Create clock
		clock = mock(Clock.class);

		// Create light-level provider
		light = mock(LightLevelProvider.class);
		when(light.level(loc)).thenReturn(Percentile.ONE);

		// Create emission controller
		controller = mock(EmissionController.class);
		when(controller.find(Set.of(Emission.LIGHT, Emission.SMOKE), loc)).thenReturn(Collections.emptyList());

		// Create perception controller
		perception = mock(PerceptionCalculator.class);
		when(perception.score(actor)).thenReturn(Percentile.HALF);
		when(perception.filter(eq(Percentile.HALF), any())).thenReturn(e -> true);

		// Create action
		action = new LookAction(clock, light, controller, perception);
	}

	@Test
	public void parsers() {
		assertNotNull(action.parsers(actor));
		assertNotNull(action.parsers(actor).parsers(AroundArgument.class));
	}

	@Test
	public void lookLocation() {
		assertEquals(Response.DISPLAY_LOCATION, action.look());
	}

	@Test
	public void lookDirection() {
		final Area area = new Area.Builder("area").view(Direction.EAST, View.of("view")).build();
		when(loc.area()).thenReturn(area);
		when(light.isDaylight()).thenReturn(true);
		final Response response = action.look(actor, Direction.EAST);
		final Response expected = Response.of(Description.of("view"));
		assertEquals(expected, response);
	}

	@Test
	public void lookDirectionDark() {
		final Response response = action.look(actor, Direction.EAST);
		assertEquals(Response.of("look.requires.light"), response);
	}

	@Test
	public void lookDirectionNone() {
		when(light.isDaylight()).thenReturn(true);
		final Response response = action.look(actor, Direction.EAST);
		assertEquals(Response.of("look.nothing"), response);
	}

	@Test
	public void lookAroundEmission() {
		final EmissionNotification emission = new EmissionNotification(Emission.LIGHT, Percentile.ONE);
		when(controller.find(Set.of(Emission.LIGHT, Emission.SMOKE), loc)).thenReturn(List.of(emission));
		final Response response = action.look(actor, LookAction.AROUND);
		final Response expected = Response.of(emission.describe());
		assertEquals(expected, response);
	}

	@Test
	public void lookAroundNotOpenTerrain() {
		when(loc.terrain()).thenReturn(Terrain.UNDERGROUND);
		assertEquals(Response.of("look.nothing"), action.look(actor, LookAction.AROUND));
	}

	@Test
	public void lookAroundDark() {
		assertEquals(Response.of("look.nothing"), action.look(actor, LookAction.AROUND));
	}

	@Test
	public void lookWindow() {
		// Create window
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("window").reset(DURATION).build();
		final Window window = new Window.Descriptor(descriptor, "curtains", View.of("view")).create();
		window.model().set(Openable.State.OPEN);

		// Look through window
		final Response response = action.look(actor, window);
		assertEquals(Response.of(Description.of("view")), response);
	}
}
