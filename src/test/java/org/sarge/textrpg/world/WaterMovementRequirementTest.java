package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.MovementController.Result;

public class WaterMovementRequirementTest extends ActionTestBase {
	private WaterMovementRequirement req;
	private Exit exit;

	@BeforeEach
	public void before() {
		req = new WaterMovementRequirement();
		exit = new Exit(Direction.EAST, Link.DEFAULT, loc);
	}

	@Test
	public void result() {
		assertEquals(Result.DEFAULT, req.result(actor, exit));
	}

	@Test
	public void waterFrozen() {
		when(loc.isWater()).thenReturn(true);
		when(loc.isFrozen()).thenReturn(true);
		assertEquals(new Result(Description.of("move.frozen.water"), 0), req.result(actor, exit));
	}

	@Test
	public void waterAlreadySwimming() {
		when(loc.isWater()).thenReturn(true);
		when(actor.model().stance()).thenReturn(Stance.SWIMMING);
		assertEquals(Result.DEFAULT, req.result(actor, exit));
	}

	@Test
	public void waterStartSwimming() {
		when(loc.isWater()).thenReturn(true);
		when(actor.isSwimEnabled()).thenReturn(true);
		assertEquals(Result.DEFAULT, req.result(actor, exit));
	}

	@Test
	public void stopSwimming() {
		when(actor.model().stance()).thenReturn(Stance.SWIMMING);
		assertEquals(Result.DEFAULT, req.result(actor, exit));
	}

	@Test
	public void waterCannotSwim() {
		when(loc.isWater()).thenReturn(true);
		assertEquals(new Result("move.cannot.swim"), req.result(actor, exit));
	}
}
