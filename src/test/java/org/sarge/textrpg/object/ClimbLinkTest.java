package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Percentile;

public class ClimbLinkTest {
	private ClimbLink link;
	private WorldObject obj;

	@BeforeEach
	public void before() {
		obj = ObjectDescriptor.fixture("climb").create();
		link = new ClimbLink(ClimbLink.DEFAULT_PROPERTIES, true, obj, true, Percentile.HALF);
	}

	@Test
	public void constructor() {
		assertEquals(Percentile.HALF, link.difficulty());
		assertEquals(true, link.isQuiet());
		assertEquals(Optional.of(obj), link.controller());
		assertEquals(Optional.of(new Description("move.invalid.direction")), link.reason(null));
		assertEquals(true, link.isEntityOnly());
		assertEquals("/dir/", link.wrap("dir"));
		assertEquals(true, link.up());
	}
}
