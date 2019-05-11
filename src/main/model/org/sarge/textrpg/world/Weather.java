package org.sarge.textrpg.world;

import static java.util.stream.Collectors.joining;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.Function;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.EnumerationIntegerMap;
import org.sarge.textrpg.util.IntegerMap;
import org.sarge.textrpg.util.MutableIntegerMap.MutableEntry;
import org.sarge.textrpg.util.Randomiser;

/**
 * Weather model.
 * <p>
 * Weather is comprised of three components defined by the {@link Component} enumeration.
 * <p>
 * The model consists of:
 * <ul>
 * <li><i>current</i> weather values which can be accessed using {@link #current()} and {@link #table(Component)} methods</li>
 * <li>historical weather values accessed via {@link #history()}</li>
 * <li>the cumulative {@link #snow()} level</li>
 * </ul>
 * The component values can be periodically randomised between a specified min/max range for each component using {@link #randomise(int)}.
 * <p>
 * Notes:
 * <ul>
 * <li>Weather component values are in the range 0..5</li>
 * <li>The initial values start at the specified minimum</li>
 * <li>The cumulative snow level increases in frozen conditions and thaws during warmer weather (see {@link Entry#isFrozen()})</li>
 * <li>The {@link #describe()} method builds a compound key for the <i>current</i> weather components, e.g. <tt>mild.dry.breezy</tt></li>
 * </ul>
 * @author Sarge
 */
public class Weather extends AbstractObject {
	public static final int MAX = 5;

	/**
	 * Weather components.
	 */
	public enum Component {
		TEMPERATURE		("frozen",	"cold",		"mild",		"warm",		"hot"),
		PRECIPITATION	("dry",		"light",	"wet",		"heavy",	"torrential"),
		WIND			("still",	"breezy",	"windy",	"gale",		"storm");

		private final String[] names;

		private Component(String... names) {
			if(names.length != MAX) throw new IllegalArgumentException();
			this.names = names;
		}
	}

	private static final Component[] COMPONENTS = Component.values().clone();

	/**
	 * No weather.
	 */
	public static final Weather NONE = new Weather() {
		@Override
		public boolean isFrozen() {
			return false;
		}

		@Override
		public void randomise(int max) {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * Weather values.
	 */
	public static class Entry extends AbstractEqualsObject {
		private final IntegerMap<Component> values;

		/**
		 * Constructor.
		 * @param values Weather values
		 */
		private Entry(IntegerMap<Component> values) {
			this.values = notNull(values);
			verify(values);
		}

		/**
		 * Looks up the level for the given weather component.
		 * @param c Weather component
		 * @return Level (0..n)
		 */
		public int get(Component c) {
			return values.get(c).get();
		}

		/**
		 * @return Snow level increment for this entry
		 */
		private int snow() {
			if(isFrozen()) {
				return values.get(Component.PRECIPITATION).get();
			}
			else {
				return -values.get(Component.TEMPERATURE).get();
			}
		}

		/**
		 * @return Whether currently frozen, i.e. {@link Component#TEMPERATURE} is zero
		 */
		private boolean isFrozen() {
			return values.get(Component.TEMPERATURE).get() == 0;
		}

		@Override
		public String toString() {
			return values.toString();
		}
	}

	/**
	 * @param level Weather component level
	 * @return Whether the given level is valid
	 */
	private static boolean isValid(int level) {
		return (level >= 0) && (level < MAX);
	}

	private final Deque<Entry> history = new ArrayDeque<>();
	private final IntegerMap<Component> min, max;
	private int snow;
	// TODO - water/wetness

	/**
	 * Default constructor for no weather.
	 */
	private Weather() {
		this(new EnumerationIntegerMap<>(Component.class), new EnumerationIntegerMap<>(Component.class));
	}

	/**
	 * Constructor.
	 * @param min Minimum values
	 * @param max Maximum values
	 */
	private Weather(IntegerMap<Component> min, IntegerMap<Component> max) {
		this.min = notNull(min);
		this.max = notNull(max);
		verify(min);
		verify(max);
		verify();
		push(min);
		updateSnow();
	}

	/**
	 * Checks min/max values.
	 */
	private void verify() {
		for(Component c : COMPONENTS) {
			if(min.get(c).get() > max.get(c).get()) {
				throw new IllegalArgumentException("Invalid min/max values: " + this);
			}
		}
	}

	/**
	 * Checks the given set of weather values.
	 */
	private static void verify(IntegerMap<Component> values) {
		if(!Arrays.stream(COMPONENTS).map(values::get).mapToInt(IntegerMap.Entry::get).allMatch(Weather::isValid)) {
			throw new IllegalArgumentException("Invalid weather entry");
		}
	}

	/**
	 * @return Cumulative snow level
	 */
	public int snow() {
		return snow;
	}

	/**
	 * @return Whether currently frozen, i.e. {@link Component#TEMPERATURE} is zero
	 */
	public boolean isFrozen() {
		return history.getFirst().values.get(Component.TEMPERATURE).get() == 0;
	}

	/**
	 * @return Weather history with most-recent first (including current entry)
	 */
	public Stream<Entry> history() {
		return history.stream();
	}

	/**
	 * Randomises weather within the specified min/max ranges and adds a new history entry.
	 * @param max Maximum length of history
	 * @see #history()
	 */
	public void randomise(int max) {
		// Randomise weather
		final EnumerationIntegerMap<Component> values = new EnumerationIntegerMap<>(Component.class);
		final Entry current = history.getFirst();
		for(Component c : COMPONENTS) {
			final int value = current.get(c);
			final int next = randomise(c, value);
			assert isValid(next);
			values.get(c).set(next);
		}

		// Update and trim history
		push(values);
		while(history.size() > max) {
			history.removeLast();
		}

		// Re-calculate cumulative snow level
		updateSnow();
		assert snow >= 0;
	}

	/**
	 * Adds a new history entry.
	 * @param values Weather component values
	 */
	private void push(IntegerMap<Component> values) {
		history.push(new Entry(values));
	}

	/**
	 * Updates snow level.
	 */
	private void updateSnow() {
		final int total = history.stream().mapToInt(Entry::snow).sum();
		snow = Math.max(0, total);
	}

	/**
	 * Randomises a weather value within the min/max values.
	 * @param c 		Component
	 * @param current	Current value
	 * @return Randomised value
	 */
	private int randomise(Component c, int current) {
		// Randomise next value up/down by one
		final int inc = Randomiser.range(3) - 1;
		final int next = current + inc;

		// Clamp to min/max
		// TODO - utility
		final int min = this.min.get(c).get();
		final int max = this.max.get(c).get();
		if(next < min) {
			return min;
		}
		else
		if(next > max) {
			return max;
		}
		else {
			return next;
		}
	}

	/**
	 * Describes this weather.
	 * @return Weather description
	 */
	public Description describe() {
		// Build compound weather description key
		final Entry current = history.getFirst();
		final Function<Component, String> mapper = c -> {
			final int level = current.values.get(c).get();
			return c.names[level];
		};
		final String weather = Arrays.stream(COMPONENTS).map(mapper).collect(joining(".")).toLowerCase();

		// Create description
		return new Description.Builder("weather.description")
			.add("weather", weather)
			.add("snow", snow) // TODO - banding table
			.build();
	}

	/**
	 * Describes the most recent change in the weather.
	 * @return Weather change description
	 */
	public Description difference() {
		// TODO - e.g. rain started/stopped, snow start/stop, warmer/colder, etc
		return new Description.Builder("weather.change.description")
			.build();
	}

	/**
	 * Builder for a weather model.
	 */
	public static class Builder {
		private final EnumerationIntegerMap<Component> min = new EnumerationIntegerMap<>(Component.class);
		private final EnumerationIntegerMap<Component> max = new EnumerationIntegerMap<>(Component.class);

		/**
		 * Sets the minimum value of the given weather component.
		 * @param c			Weather component
		 * @param value		Minimum value (0..n)
		 */
		public Builder min(Component c, int value) {
			// Set minimum
			min.get(c).set(value);

			// Ensure maximum
			final MutableEntry entry = max.get(c);
			if(value > entry.get()) {
				entry.set(value);
			}

			return this;
		}

		/**
		 * Sets the maximum value of the given weather component.
		 * @param c			Weather component
		 * @param value		Maximum value (0..n)
		 */
		public Builder max(Component c, int value) {
			max.get(c).set(value);
			return this;
		}

		/**
		 * Constructs a new weather model.
		 * @return Weather model
		 */
		public Weather build() {
			return new Weather(min, max);
		}
	}
}
