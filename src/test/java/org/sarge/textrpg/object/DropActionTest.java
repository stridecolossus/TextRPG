package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.Location;
import org.sarge.textrpg.world.Property;
import org.sarge.textrpg.world.RiverController;
import org.sarge.textrpg.world.Terrain;

public class DropActionTest extends ActionTestBase {
	private DropAction action;
	private ObjectController controller;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("object").decay(DURATION).build();
		obj = descriptor.create();
		obj.parent(actor);
		controller = mock(ObjectController.class);
		action = new DropAction(controller, mock(RiverController.class));
	}

	@Test
	public void drop() throws ActionException {
		final Response response = action.drop(actor, obj);
		assertEquals(Response.of(new Description("drop.object.response", "object")), response);
		assertEquals(loc, obj.parent());
		verify(controller).decay(obj);
	}

	@Test
	public void dropFloorlessLocation() throws ActionException {
		// Create bottom location
		final Location bottom = mock(Location.class);
		when(bottom.name()).thenReturn("bottom");
		when(bottom.terrain()).thenReturn(Terrain.DESERT);
		when(bottom.contents()).thenReturn(new Contents());

		// Create floorless exit
		final ExitMap exits = ExitMap.of(Exit.of(Direction.DOWN, bottom));
		when(loc.isProperty(Property.FLOORLESS)).thenReturn(true);
		when(loc.exits()).thenReturn(exits);

		// Drop object and check dropped to bottom
		assertEquals(Response.of(new Description("drop.object.response", "object")), action.drop(actor, obj));
		assertEquals(bottom, obj.parent());
	}

	@Test
	public void dropWater() throws ActionException {
		when(loc.isWater()).thenReturn(true);
		assertEquals(Response.of(new Description("drop.object.water", "object")), action.drop(actor, obj));
		assertEquals(false, obj.isAlive());
	}

	@Test
	public void dropEquipped() throws ActionException {
		actor.contents().equipment().equip(obj, Slot.ARMS);
		assertEquals(Response.of(new Description("drop.object.equipped", "object")), action.drop(actor, obj));
	}

	@Test
	public void dropAll() throws ActionException {
		action.drop(actor, desc -> true);
	}

	@Test
	public void dropAllNone() throws ActionException {
		TestHelper.expect("drop.none.matched", () -> action.drop(actor, desc -> false));
	}
}
