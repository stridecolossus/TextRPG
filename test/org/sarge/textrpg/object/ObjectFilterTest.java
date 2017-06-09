package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.common.Value;
import org.sarge.textrpg.entity.DamageEffect;
import org.sarge.textrpg.entity.Effect;

public class ObjectFilterTest {
	@Test
	public void filter() {
		final ObjectFilter filter = new ObjectFilter("name", desc -> true);
		assertEquals(true, filter.predicate().test(null));
		assertEquals("name", filter.toString());
	}
	
	@Test
	public void all() {
		assertEquals(true, ObjectFilter.ALL.predicate().test(null));
		assertEquals("object.all", ObjectFilter.ALL.toString());
	}
	
	@Test
	public void slot() {
		final ObjectDescriptor descriptor = new ObjectDescriptor.Builder("equipment").slot(DeploymentSlot.ARMS).build();
		final ObjectFilter filter = ObjectFilter.create(DeploymentSlot.ARMS);
		assertEquals(true, filter.predicate().test(descriptor));
		assertEquals("slot.arms", filter.toString());
	}
	
	@Test
	public void damageType() {
		final DamageEffect damage = new DamageEffect(DamageType.COLD, Value.ONE, false);
		final Weapon weapon = new Weapon(new ObjectDescriptor("weapon"), 1, 1, damage, Effect.NONE, null);
		final ObjectFilter filter = ObjectFilter.create(DamageType.COLD);
		assertEquals(true, filter.predicate().test(weapon));
		assertEquals("filter.cold", filter.toString());
	}
}
