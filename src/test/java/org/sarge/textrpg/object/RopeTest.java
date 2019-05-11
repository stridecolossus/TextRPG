package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class RopeTest {
	private Rope rope;
	private Rope.Anchor anchor;

	@BeforeEach
	public void before() {
		rope = new Rope(new Rope.Descriptor(new DurableObject.Descriptor(ObjectDescriptor.of("rope"), 1), 2, true));
		anchor = new Rope.Anchor("anchor", ObjectDescriptor.Characteristics.PLACEMENT_DEFAULT);
	}

	@Test
	public void constructor() {
		assertEquals("rope", rope.name());
		assertEquals(0, rope.value());
		assertEquals(0, rope.weight());
		assertEquals(Size.NONE, rope.size());
		assertEquals(Percentile.ONE, rope.visibility());
		assertEquals(0, rope.wear());
		assertEquals(false, rope.isBroken());
		assertEquals(false, rope.isQuiet());
	}

	@Test
	public void descriptor() {
		assertNotNull(rope.descriptor());
		assertEquals(false, rope.descriptor().isFixture());
		assertEquals(false, rope.descriptor().isResetable());
		assertEquals(2, rope.descriptor().length());
		assertEquals(true, rope.descriptor().isMagical());
	}

	@Test
	public void anchor() {
		assertEquals("anchor", anchor.name());
		assertEquals(true, anchor.descriptor().isFixture());
		assertEquals(false, anchor.isQuiet());
	}

	@Test
	public void describe() throws ActionException {
		rope.attach(anchor);
		final Description description = rope.describe(false, null);
		final Description.Entry anchor = description.get("rope.anchor");
		assertNotNull(anchor);
		assertEquals("anchor", anchor.argument());
	}

	@Test
	public void attach() throws ActionException {
		rope.attach(anchor);
		assertEquals(anchor, rope.anchor());
		assertEquals(true, anchor.isQuiet());
		assertEquals("rope.attached", rope.key(false));
	}

	@Test
	public void attachAlreadyAttached() throws ActionException {
		rope.attach(anchor);
		TestHelper.expect(ActionException.class, "rope.already.attached", () -> rope.attach(anchor));
	}

	@Test
	public void attachAnchorOccupied() throws ActionException {
		final Rope other = new Rope(new Rope.Descriptor(new DurableObject.Descriptor(ObjectDescriptor.of("rope"), 1), 2, true));
		other.attach(anchor);
		TestHelper.expect(ActionException.class, "rope.anchor.occupied", () -> rope.attach(anchor));
	}

	@Test
	public void attachRopeBroken() throws ActionException {
		rope.use();
		TestHelper.expect(ActionException.class, "rope.broken", () -> rope.attach(anchor));
	}

	@Test
	public void remove() throws ActionException {
		rope.attach(anchor);
		rope.remove();
		assertEquals(null, rope.anchor());
		assertEquals(true, rope.isDamaged());
	}

	@Test
	public void removeNotAttached() {
		TestHelper.expect(ActionException.class, "rope.not.attached", () -> rope.remove());
	}

	@Test
	public void destroy() throws ActionException {
		rope.parent(TestHelper.parent());
		rope.attach(anchor);
		rope.destroy();
		assertEquals(null, rope.anchor());
	}
}
