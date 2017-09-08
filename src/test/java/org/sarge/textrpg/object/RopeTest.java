package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.object.Rope.Anchor;
import org.sarge.textrpg.object.Rope.Descriptor;

public class RopeTest extends ActionTest {
	private Rope rope;
	private Anchor anchor;

	@Before
	public void before() throws ActionException {
		rope = new Rope(new Descriptor(new ObjectDescriptor("rope"), 1, 2, true));
		anchor = new Anchor(new ObjectDescriptor("anchor"));
		rope.setParent(actor);
	}

	@Test
	public void constructor() {
		assertEquals(Optional.empty(), rope.anchor());
		assertEquals(false, anchor.isAttached());
		assertEquals(2, rope.length());
		assertEquals(null, anchor.rope());
	}

	@Test
	public void describe() {
		final Description desc = rope.describe();
		assertEquals("{rope}", desc.get("name"));
		assertEquals("2", desc.get("length"));
		assertEquals(null, desc.get("anchor"));
	}

	@Test
	public void attach() throws ActionException {
		rope.attach(actor, anchor);
		assertEquals(Optional.of(anchor), rope.anchor());
		assertEquals(true, rope.anchor().get().isAttached());
		assertEquals("{anchor}", rope.describe().get("anchor"));
		assertEquals(rope, anchor.rope());
	}

	@Test
	public void attachAlreadyAttached() throws ActionException {
		rope.attach(actor, anchor);
		expect("rope.already.attached");
		rope.attach(actor, anchor);
	}

	@Test
	public void attachAlreadyOccupied() throws ActionException {
		final Rope other = new Rope(new Descriptor(new ObjectDescriptor("rope"), 1, 2, false));
		other.setParent(actor);
		other.attach(actor, anchor);
		expect("rope.anchor.occupied");
		rope.attach(actor, anchor);
	}

	@Test
	public void attachBroken() throws ActionException {
		rope.use();
		expect("rope.attach.broken");
		rope.attach(actor, anchor);
	}

	@Test
	public void takeAttached() throws ActionException {
		rope.attach(actor, anchor);
		expect("take.rope.attached");
		rope.take(actor);
	}

	@Test
	public void remove() throws ActionException {
		rope.attach(actor, anchor);
		rope.remove(actor);
		assertEquals(Optional.empty(), rope.anchor());
		assertEquals(false, anchor.isAttached());
	}

	@Test
	public void removeNotAttached() throws ActionException {
		expect("rope.not.attached");
		rope.remove(actor);
	}

	@Test
	public void removeDifferentLocation() throws ActionException {
		rope.attach(actor, anchor);
		rope.setParent(super.createLocation());
		expect("rope.invalid.location");
		rope.remove(actor);
	}

	@Test
	public void pull() throws ActionException {
		rope.attach(actor, anchor);
		rope.setParent(super.createLocation());
		rope.pull(actor);
		assertEquals(Optional.empty(), rope.anchor());
		assertEquals(false, anchor.isAttached());
		assertEquals(actor, rope.parent());
	}

	@Test
	public void pullInvalidLocation() throws ActionException {
		rope.attach(actor, anchor);
		expect("rope.invalid.location");
		rope.pull(actor);
	}

	@Test
	public void pullNotMagical() throws ActionException {
		rope = new Rope(new Descriptor(new ObjectDescriptor("rope"), 1, 2, false));
		rope.setParent(actor);
		rope.attach(actor, anchor);
		rope.setParent(super.createLocation());
		expect("interact.nothing");
		rope.pull(actor);
	}
}
