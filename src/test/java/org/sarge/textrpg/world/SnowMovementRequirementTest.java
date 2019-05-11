package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.MovementController.Result;

public class SnowMovementRequirementTest extends ActionTestBase {
	private SnowMovementRequirement req;
	private SnowModel model;
	private Exit exit;

	@BeforeEach
	public void before() {
		model = mock(SnowModel.class);
		req = new SnowMovementRequirement(model);
		exit = new Exit(Direction.EAST, Link.DEFAULT, loc);
	}

	/**
	 * Adds some snow.
	 */
	private void add() {
		when(model.snow(loc)).thenReturn(3);
	}

	@Test
	public void result() {
		assertEquals(Result.DEFAULT, req.result(actor, exit));
	}

	@Test
	public void snow() {
		req.setSnowModifier(2);
		add();
		assertEquals(new Result(Description.of("move.snow.level"), 2 * 3), req.result(actor, exit));
	}

	@Test
	public void snowBelowThreshold() {
		req.setSnowThreshold(4);
		add();
		assertEquals(Result.DEFAULT, req.result(actor, exit));
	}

	@Test
	public void snowTooDeep() {
		req.setMaxSnowLevel(2);
		add();
		assertEquals(new Result("move.too.deep"), req.result(actor, exit));
	}
}
