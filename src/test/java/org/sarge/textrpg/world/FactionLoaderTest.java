package org.sarge.textrpg.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.PeriodModel;
import org.sarge.textrpg.util.Registry;

public class FactionLoaderTest {
	private FactionLoader loader;
	private Registry<Calendar> calendars;
	private PeriodModel.Factory factory;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		// Create calendar registry
		calendars = mock(Registry.class);
		when(calendars.get(anyString())).thenReturn(mock(Calendar.class));

		// Create opening-times factory
		factory = mock(PeriodModel.Factory.class);
		when(factory.create(any())).thenReturn(mock(PeriodModel.class));

		// Create loader
		loader = new FactionLoader(calendars, factory);
	}

	@Test
	public void load() {
		final Element xml = new Element.Builder("xml")
			.attribute("name", "name")
			.attribute("alignment", "evil")
			.build();

		final Faction faction = loader.load(xml, Area.ROOT);
		assertNotNull(faction);
		assertEquals("name", faction.name());
		assertEquals(Alignment.EVIL, faction.alignment());
		assertEquals(Area.ROOT, faction.area());
		assertNotNull(faction.opening());
		assertNotNull(faction.calendar());
	}
}
