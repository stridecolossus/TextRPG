package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.util.Description;

public class LinkTest {
	private Link link;

	@BeforeEach
	public void before() {
		link = Link.DEFAULT;
	}

	@Test
	public void constructor() {
		assertEquals(Size.NONE, link.size());
		assertEquals(Route.NONE, link.route());
		assertEquals(1f, link.modifier(), 0.0001f);
		assertEquals(Optional.empty(), link.controller());
		assertEquals(true, link.isTraversable());
		assertEquals(false, link.isEntityOnly());
		assertEquals(Optional.empty(), link.reason(null));
		assertEquals(Optional.empty(), link.message());
		assertEquals("default", link.key());
	}

	@Test
	public void wrap() {
		final String dir = "dir";
		assertEquals(dir, link.wrap(dir));
	}

	@Test
	public void describe() {
		final var builder = new Description.Builder("key");
		link.describe(builder);
		assertEquals(Description.of("key"), builder.build());
	}

	@Test
	public void invert() {
		assertEquals(link, link.invert());
	}
}
