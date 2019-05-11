package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.BandingTable;

public class ViewTest {
	@Test
	public void none() {
		assertEquals("view.none", View.NONE.describe(null));
	}

	@Test
	public void simple() {
		final View view = View.of("view");
		assertNotNull(view);
		assertEquals("view", view.describe(null));
	}

	@Test
	public void table() {
		// Builds table
		final BandingTable<LocalTime> table = new BandingTable.Builder<LocalTime>()
			.add(LocalTime.of(12, 0), "morning")
			.add(LocalTime.MAX, "afternoon")
			.build();

		// Create view
		final View view = View.of(table);
		assertNotNull(view);

		// Check view in the morning
		assertEquals("morning", view.describe(LocalTime.of(0, 0)));
		assertEquals("morning", view.describe(LocalTime.of(8, 0)));
		assertEquals("morning", view.describe(LocalTime.of(12, 0)));

		// Check view in the afternoon
		assertEquals("afternoon", view.describe(LocalTime.of(13, 0)));
		assertEquals("afternoon", view.describe(LocalTime.MAX));
	}

	@Test
	public void loadSimpleView() {
		final Element xml = new Element.Builder("xml").attribute("view", "view").build();
		final View view = View.load(xml);
		assertNotNull(view);
		assertEquals("view", view.describe(null));
	}

	@Test
	public void loadTableView() {
		final Element xml = new Element.Builder("xml")
			.child("view")
				.attribute("view", "view")
				.attribute("time", "08:00")
				.end()
			.build();
		final View view = View.load(xml);
		assertNotNull(view);
		assertEquals("view", view.describe(LocalTime.MIN));
	}
}
