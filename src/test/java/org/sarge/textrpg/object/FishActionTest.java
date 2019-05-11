package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Area;
import org.sarge.textrpg.world.Property;

public class FishActionTest extends ActionTestBase {
	private FishAction action;

	@BeforeEach
	public void before() {
		action = new FishAction(skill);
	}

	private void init() {
		when(loc.isProperty(Property.FISH)).thenReturn(true);
	}

	@Test
	public void fish() throws ActionException {
		// Add fish
		final LootFactory factory = LootFactory.of(ObjectDescriptor.of("fish"), 1);
		final Area area = new Area.Builder("area").resource(FishAction.FISH, factory).build();
		when(loc.area()).thenReturn(area);
		init();
		addRequiredSkill();

		// Start fishing
		final Response response = action.fish(actor);

		// Complete catch
		final Response result = complete(response);
		// TODO

	}

	@Test
	public void fishInvalidLocation() throws ActionException {
		TestHelper.expect("fish.invalid.location", () -> action.fish(actor));
	}

	@Test
	public void fishFailedCatch() throws ActionException {
		init();
		final Response response = action.fish(actor);
		assertEquals(Response.of("fish.failed"), complete(response));
	}

	@Test
	public void fishEmptyCatch() throws ActionException {
		addRequiredSkill();
		init();
		final Response response = action.fish(actor);
		assertEquals(Response.of("fish.failed"), complete(response));
	}
}
