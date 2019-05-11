package org.sarge.textrpg.common;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.util.BandingTable;

/**
 * View from a vantage-point or window.
 * @author Sarge
 */
@FunctionalInterface
public interface View {
	/**
	 * Determines the view description key for the given time-of-day.
	 * @param time Time-of-day
	 * @return View
	 */
	String describe(LocalTime time);

	/**
	 * Empty view.
	 */
	View NONE = ignore -> "view.none";

	/**
	 * Creates a simple view (irrespective of the time-of-day).
	 * @param view View description key
	 * @return Simple view
	 */
	static View of(String view) {
		return ignore -> view;
	}

	/**
	 * Creates a view based on a time-of-day table.
	 * @param table Table
	 * @return Table view
	 */
	static View of(BandingTable<LocalTime> table) {
		return table::map;
	}

	/**
	 * Loads a view - either a simple <i>view</i> attribute or a table loaded from a set of <i>view</i> child elements.
	 * @param xml XML
	 * @return View
	 */
	static View load(Element xml) {
		return xml.attribute("view").optional().map(View::of).orElseGet(() -> loadViewTable(xml));
	}

	/**
	 * Loads a view table.
	 * @param xml XML
	 * @return View table
	 */
	private static View loadViewTable(Element xml) {
		// Load table
		final var builder = new BandingTable.Builder<LocalTime>();
		final Consumer<Element> mapper = e -> {
			final String name = e.attribute("view").toText();
			final LocalTime time = e.attribute("time").toValue(LocalTime::parse);
			builder.add(time, name);
		};
		try {
			xml.children("view").forEach(mapper);
		}
		catch(DateTimeParseException e) {
			throw xml.exception(e);
		}

		// Build view
		final BandingTable<LocalTime> table = builder.build();
		return View.of(table);
	}
}
