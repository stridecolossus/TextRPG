package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FixtureTest {
	@Test
	public void constructor() {
		final ObjectDescriptor fixture = new Fixture(new ObjectDescriptor("fixture"));
		assertEquals(true, fixture.isFixture());
		assertEquals("stands", fixture.create().getFullDescriptionKey());
	}
}
