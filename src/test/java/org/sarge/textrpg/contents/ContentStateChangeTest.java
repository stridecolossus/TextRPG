package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.contents.ContentStateChange.Type;
import org.sarge.textrpg.util.Description;

public class ContentStateChangeTest {
	private ContentStateChange notification;

	@BeforeEach
	public void before() {
		notification = ContentStateChange.of(Type.OTHER, new Description("key"));
	}

	@Test
	public void of() {
		assertNotNull(notification);
		assertEquals(Type.OTHER, notification.type());
		assertEquals(new Description("key"), notification.describe());
	}

	@Test
	public void equals() {
		assertEquals(notification, notification);
		assertTrue(notification.equals(notification));
		assertFalse(notification.equals(null));
		assertFalse(notification.equals(ContentStateChange.of(Type.OTHER, new Description("other"))));
		assertFalse(notification.equals(ContentStateChange.LIGHT_MODIFIED));
	}

	@Test
	public void constructorInvalidLightLevel() {
		assertThrows(IllegalArgumentException.class, () -> ContentStateChange.of(Type.LIGHT, new Description("key")));
	}
}
