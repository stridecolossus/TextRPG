package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.OpeningTimes;
import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.PeriodModel;
import org.sarge.textrpg.util.Registry;
import org.springframework.stereotype.Service;

/**
 * Loader for factions.
 * @author Sarge
 */
@Service
public class FactionLoader {
	private final Registry<Calendar> calendars;
	private final PeriodModel.Factory factory;

	/**
	 * Constructor.
	 * @param calendars		Calendars
	 * @param factory		Period model factory
	 */
	public FactionLoader(Registry<Calendar> calendars, PeriodModel.Factory factory) {
		this.calendars = notNull(calendars);
		this.factory = notNull(factory);
	}

	/**
	 * Loads a faction descriptor.
	 * @param xml 		XML
	 * @param area		Area
	 * @return Faction
	 */
	public Faction load(Element xml, Area area) {
		final String name = xml.attribute("name").toText();
		final Alignment alignment = xml.attribute("alignment").toValue(Alignment.CONVERTER);
		final PeriodModel<OpeningTimes> times = factory.create(OpeningTimes.load(xml));
		final Calendar calendar = calendars.get(xml.attribute("calendar").toText("shire.reckoning")); // TODO - "stewards.reckoning"));
		return new Faction(name, alignment, area, times, calendar);
	}
}
