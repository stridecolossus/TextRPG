package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class SmokeActionTest extends ActionTestBase {
	private SmokeAction action;
	private WorldObject weed;

	@BeforeEach
	public void before() {
		action = new SmokeAction(DURATION);
		weed = new ObjectDescriptor.Builder("weed").category("pipe.weed").build().create();
		weed.parent(actor);
	}

	@Test
	public void smokeWeed() throws ActionException {
		// Start smoking
		final Response response = action.smoke(actor, weed);
		assertNotNull(response);
		assertTrue(response.induction().isPresent());

		// Check induction
		final Induction.Descriptor descriptor = response.induction().get().descriptor();
		assertEquals(true, descriptor.isFlag(Induction.Flag.SPINNER));
		assertEquals(false, descriptor.isFlag(Induction.Flag.REPEATING));

		// Complete induction
		final Response result = complete(response);
		final Response expected = Response.of(new Description("action.smoke.result", weed.name()));
		assertEquals(expected, result);

		// Check weed consumed
		assertEquals(false, weed.isAlive());
	}

	@Test
	public void smoke() throws ActionException {
		action.smoke(actor);
	}

	@Test
	public void smokeRequiresWeed() throws ActionException {
		weed.destroy();
		TestHelper.expect("smoke.requires.weed", () -> action.smoke(actor));
	}
}
