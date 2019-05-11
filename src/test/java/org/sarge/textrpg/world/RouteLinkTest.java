package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RouteLinkTest {
	@ParameterizedTest
	@EnumSource(value=Route.class, mode=EnumSource.Mode.EXCLUDE, names={"NONE"})
	public void of(Route route) {
		final RouteLink link = RouteLink.of(route);
		assertNotNull(link);
		assertEquals(route, link.route());
	}

	@Test
	public void invalid() {
		assertThrows(IllegalArgumentException.class, () -> RouteLink.of(Route.NONE));
	}
}
