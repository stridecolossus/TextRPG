package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EntityValueTest {
	@Test
	public void getMaximumValue() {
		assertEquals(EntityValue.MAX_HEALTH, EntityValue.HEALTH.getMaximumValue().get());
		assertEquals(EntityValue.MAX_POWER, EntityValue.POWER.getMaximumValue().get());
		assertEquals(EntityValue.MAX_STAMINA, EntityValue.STAMINA.getMaximumValue().get());
	}
}
