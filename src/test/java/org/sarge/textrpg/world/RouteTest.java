package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RouteTest {
	@Test
	public void wrap() {
		assertEquals("dir", Route.NONE.wrap("dir"));
		assertEquals(")dir(", Route.BRIDGE.wrap("dir"));
		assertEquals("-dir-", Route.TRAIL.wrap("dir"));
	}

	@ParameterizedTest
	@EnumSource(value=Route.class, mode=EnumSource.Mode.EXCLUDE, names={"NONE", "BRIDGE", "LADDER", "FORD"})
	public void isFollowRoute(Route route) {
		assertEquals(true, route.isFollowRoute());
	}

	@ParameterizedTest
	@EnumSource(value=Route.class, names={"NONE", "BRIDGE", "LADDER", "FORD"})
	public void isNotFollowRoute(Route route) {
		assertEquals(false, route.isFollowRoute());
	}
}
