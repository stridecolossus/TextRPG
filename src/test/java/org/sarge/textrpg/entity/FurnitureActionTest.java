package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;

public class FurnitureActionTest extends ActionTestBase {
	private FurnitureAction action;

	@BeforeEach
	public void before() {
		action = new FurnitureAction();
	}

	private static Furniture create(Stance stance) {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("furniture").size(Size.MEDIUM).build();
		return new Furniture.Descriptor(descriptor, Set.of(stance), 1, "in").create();
	}

	@Test
	public void sit() throws ActionException {
		final Furniture furniture = create(Stance.RESTING);
		final Response response = action.sit(actor, furniture);
		assertEquals(Response.of(new Description("action.furniture.resting", "furniture")), response);
		verify(actor).parent(furniture);
		verify(actor.model()).stance(Stance.RESTING);
	}

	@Test
	public void sleep() throws ActionException {
		final Furniture furniture = create(Stance.SLEEPING);
		final Response response = action.sleep(actor, furniture);
		assertEquals(Response.of(new Description("action.furniture.sleeping", "furniture")), response);
		verify(actor).parent(furniture);
		verify(actor.model()).stance(Stance.SLEEPING);
	}

	@Test
	public void invalidStance() throws ActionException {
		final Furniture furniture = create(Stance.RESTING);
		TestHelper.expect("furniture.cannot.sleeping", () -> action.sleep(actor, furniture));
	}

	@Test
	public void sizeConstraint() throws ActionException {
		final Furniture furniture = create(Stance.RESTING);
		when(actor.size()).thenReturn(Size.LARGE);
		TestHelper.expect("furniture.size.constraint", () -> action.sit(actor, furniture));
	}

	@Test
	public void capacityExceeded() throws ActionException {
		final Furniture furniture = create(Stance.RESTING);
		ObjectDescriptor.of("occupant").create().parent(furniture);
		TestHelper.expect("furniture.max.occupants", () -> action.sit(actor, furniture));
	}
}
