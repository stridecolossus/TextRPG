package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;

public class OpeningTimesTest {
	private OpeningTimes open;

	@BeforeEach
	public void before() {
		open = new OpeningTimes(LocalTime.of(8, 0), true);
	}

	@Test
	public void constructor() {
		assertEquals(LocalTime.of(8, 0), open.start());
		assertEquals(true, open.isOpen());
	}

	@Test
	public void load() {
		final Element xml = new Element.Builder("xml")
			.child("opening-time")
				.attribute("open", "08:00")
				.attribute("close", "12:00")
				.end()
			.build();

		final List<OpeningTimes> result = OpeningTimes.load(xml);
		final OpeningTimes close = new OpeningTimes(LocalTime.of(12, 0), false);
		assertEquals(List.of(open, close), result);
	}
}
