package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.world.MovementController.Result;

public class DefaultMovementRequirementTest extends ActionTestBase {
	private DefaultMovementRequirement req;
	private Exit exit;

	@BeforeEach
	public void before() {
		req = new DefaultMovementRequirement();
		exit = new Exit(Direction.EAST, RouteLink.of(Route.LANE), loc);
	}

	@Test
	public void result() {
		req.setTerrainModifier(terrain -> 2);
		req.setRouteModifier(route -> 3f);
		assertEquals(new Result(null, 2 * 3), req.result(actor, exit));
	}

	@Test
	public void resultSneaking() {
		req.setSneakModifier(2);
		when(actor.model().stance()).thenReturn(Stance.SNEAKING);
		assertEquals(new Result(null, 2), req.result(actor, exit));
	}
}
