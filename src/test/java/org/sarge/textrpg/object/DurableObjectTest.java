package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;

public class DurableObjectTest {
	private DurableObject obj;

	@BeforeEach
	public void before() {
		final Material mat = new Material.Builder("mat").strength(1).damaged(Damage.Type.COLD).build();
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("tool").material(mat).build();
		obj = new DurableObject.Descriptor(descriptor, 2).create();
	}

	@Test
	public void constructor() {
		assertEquals("tool", obj.name());
		assertEquals(0, obj.wear());
		assertEquals(Percentile.ZERO, obj.condition());
		assertEquals(false, obj.isDamaged());
		assertEquals(false, obj.isBroken());
	}

	@Test
	public void use() throws ActionException {
		obj.use();
		assertEquals(1, obj.wear());
		assertEquals(Percentile.HALF, obj.condition());
		assertEquals(true, obj.isDamaged());
		assertEquals(false, obj.isBroken());
	}

	@Test
	public void useBroken() throws ActionException {
		obj.use();
		obj.use();
		assertEquals(Percentile.ONE, obj.condition());
		assertThrows(IllegalStateException.class, obj::use);
	}

	@Test
	public void repair() throws ActionException {
		obj.use();
		obj.repair();
		assertEquals(0, obj.wear());
	}

	@Test
	public void repairNotBroken() throws ActionException {
		TestHelper.expect(IllegalStateException.class, "Not damaged", obj::repair);
	}

	@Test
	public void repairPartial() {
		obj.use();
		obj.use();
		obj.repair(1);
		assertEquals(1, obj.wear());
	}

	@Test
	public void repairPartialNotBroken() throws ActionException {
		TestHelper.expect(IllegalStateException.class, "Not damaged", () -> obj.repair(1));
	}
}
