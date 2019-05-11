package org.sarge.textrpg.common;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.PeriodModel;

/**
 * Set of opening times for a shop or town-gate.
 * @author Sarge
 */
public final class OpeningTimes extends AbstractEqualsObject implements PeriodModel.Period {
	private final LocalTime start;
	private final boolean open;

	/**
	 * Constructor.
	 * @param start			Start time
	 * @param open			Whether open or closed
	 */
	public OpeningTimes(LocalTime start, boolean open) {
		this.start = notNull(start);
		this.open = open;
	}

	@Override
	public LocalTime start() {
		return start;
	}

	/**
	 * @return Whether this is an opening or closing period
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Loads a set of opening times.
	 * @param xml XML
	 * @return Opening times
	 */
	public static List<OpeningTimes> load(Element xml) {
		return xml.children("opening-time").flatMap(OpeningTimes::loadPeriod).collect(toList());
	}

	/**
	 * Loads a pair of open/close times.
	 */
	private static Stream<OpeningTimes> loadPeriod(Element xml) {
		final OpeningTimes open = load(xml, true);
		final OpeningTimes close = load(xml, false);
		return Stream.of(open, close);
	}

	/**
	 * Loads an open/closing time.
	 */
	private static OpeningTimes load(Element xml, boolean open) {
		final String name = open ? "open" : "close";
		final LocalTime start = LocalTime.parse(xml.attribute(name).toText());
		return new OpeningTimes(start, open);
	}
}
