package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class DiceActionTest extends ActionTestBase {
	private DiceAction action;

	@BeforeEach
	public void before() {
		action = new DiceAction(ArgumentFormatter.PLAIN);
	}

	@Test
	public void roll() {
		// Roll dice
		final Response response = action.roll();
		assertNotNull(response);
		assertEquals(1, response.responses().count());

		// Check response
		final Description description = response.responses().iterator().next();
		assertEquals("action.roll.dice", description.key());
		assertNotNull(description.get("side"));
	}

	@Test
	public void flip() throws ActionException {
		// Add a coin
		actor.settings().set(PlayerSettings.Setting.CASH, 1);

		// Flip coin
		final Response response = action.flip(actor);
		assertNotNull(response);
		assertEquals(1, response.responses().count());

		// Check response
		final Description description = response.responses().iterator().next();
		assertEquals("action.flip.coin", description.key());
		assertNotNull(description.get("side"));
	}

	@Test
	public void flipSkint() throws ActionException {
		TestHelper.expect("flip.requires.coin", () -> action.flip(actor));
	}
}
