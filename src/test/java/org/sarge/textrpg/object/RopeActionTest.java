package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class RopeActionTest extends ActionTestBase {
	private RopeAction action;
	private Rope rope;
	private Rope.Anchor anchor;

	@BeforeEach
	public void before() {
		action = new RopeAction();
		anchor = mock(Rope.Anchor.class);
		rope = new Rope.Descriptor(new DurableObject.Descriptor(ObjectDescriptor.of("rope"), 1), 42, true).create();
	}

	@Test
	public void attach() throws ActionException {
		assertEquals(Response.OK, action.attach(actor, rope, anchor));
		assertEquals(anchor, rope.anchor());
		assertEquals(loc, rope.parent());
	}

	@Test
	public void remove() throws ActionException {
		rope.parent(loc);
		rope.attach(anchor);
		action.detach(actor, rope);
		assertEquals(actor, rope.parent());
	}

	@Test
	public void removeInvalidLocation() throws ActionException {
		TestHelper.expect("rope.remove.invalid", () -> action.detach(actor, rope));
	}

	@Test
	public void pull() throws ActionException {
		rope.attach(anchor);
		action.pull(actor, rope);
		assertEquals(actor, rope.parent());
	}

	@Test
	public void pullInvalidLocation() throws ActionException {
		rope.attach(anchor);
		rope.parent(loc);
		TestHelper.expect("rope.pull.invalid", () -> action.pull(actor, rope));
	}

	@Test
	public void pullNotMagical() throws ActionException {
		rope = new Rope.Descriptor(new DurableObject.Descriptor(ObjectDescriptor.of("rope"), 1), 42, false).create();
		assertEquals(Response.of("rope.pull.nothing"), action.pull(actor, rope));
	}
}
