package org.sarge.textrpg.contents;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;

public class LimitsMapLoaderTest {
	private LimitsMapLoader loader;

	@BeforeEach
	public void before() {
		loader = new LimitsMapLoader();
	}

	@Test
	public void limits() {
		// Build XML descriptor
		final Element xml = new Element.Builder("limits")
			.child("capacity")
				.attribute("reason", "reason.capacity")
				.attribute("capacity", 1)
				.end()
			.child("weight")
				.attribute("reason", "reason.weight")
				.attribute("weight", 2)
				.end()
			.child("size")
				.attribute("reason", "reason.size")
				.attribute("size", Size.MEDIUM)
				.end()
			.child("category")
				.attribute("reason", "reason.category")
				.attribute("cat", "cat")
				.end()
			.build();

		// Load limits
		final LimitsMap limits = loader.load(xml);
		assertNotNull(limits);
	}
}
