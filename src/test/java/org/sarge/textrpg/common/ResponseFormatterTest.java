package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.DescriptionFormatter;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.world.LightLevelProvider;
import org.sarge.textrpg.world.LocationDescriptionBuilder;

public class ResponseFormatterTest {
	private ResponseFormatter formatter;
	private DescriptionFormatter desc;
	private LightLevelProvider light;
	private LocationDescriptionBuilder builder;
	private PlayerCharacter actor;

	@BeforeEach
	public void before() {
		desc = mock(DescriptionFormatter.class);
		when(desc.format(any(), any())).thenReturn("whatever");
		light = mock(LightLevelProvider.class);
		builder = mock(LocationDescriptionBuilder.class);
		formatter = new ResponseFormatter(desc, light, builder);
		actor = mock(PlayerCharacter.class);
		when(actor.settings()).thenReturn(new PlayerSettings());
	}

	@Test
	public void formatEmpty() {
		assertThrows(IllegalArgumentException.class, () -> formatter.format(actor, NameStore.EMPTY, Response.EMPTY));
	}

	@Test
	public void formatDefaultResponse() {
		assertEquals("ok", formatter.format(actor, NameStore.EMPTY, Response.OK));
	}

	@Test
	public void formatResponse() {
		final var response = new Response.Builder().add(Description.of("key")).build();
		formatter.format(actor, NameStore.EMPTY, response);
		verify(desc).format(Description.of("key"), NameStore.EMPTY);
	}

	@Test
	public void formatDisplayLocation() {
		when(light.isAvailable(null)).thenReturn(true);
		formatter.format(actor, NameStore.EMPTY, Response.DISPLAY_LOCATION);
		verify(builder).build(actor, false);
	}

	@Test
	public void formatDisplayLocationDark() {
		formatter.format(actor, NameStore.EMPTY, Response.DISPLAY_LOCATION);
		verifyZeroInteractions(builder);
	}
}
