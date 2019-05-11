package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Description.Builder;

public class NotificationTest extends ActionTestBase {
	private Notification notification;

	@BeforeEach
	public void before() {
		notification = new Notification("key", actor) {
			@Override
			public void handle(Handler handler, Entity entity) {
				// Does nowt
			}

			@Override
			protected void describe(Builder builder) {
				// Does nowt
			}
		};
	}

	@Test
	public void constructor() {
		assertEquals(actor, notification.actor());
	}

	@Test
	public void describe() {
		final Description expected = new Description.Builder("key").name("actor").build();
		assertEquals(expected, notification.describe());
	}
}
