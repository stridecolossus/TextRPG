package org.sarge.textrpg.common;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Check;

/**
 * Clock listener to toggle the state of something such as shop opening times.
 * @author Sarge
 */
public class ToggleListener implements Clock.Listener {
    /**
     * Open/closing times for this toggle.
     */
    public static final class Entry {
        private final int open, close;

        /**
         * Constructor.
         * @param open      Opening hour 0..23
         * @param close     Closing hour 0..23
         */
        public Entry(int open, int close) {
            this.open = Check.range(open, 0, 23);
            this.close = Check.range(close, 0, 23);
        }

        @Override
        public String toString() {
            return open + "," + close;
        }
    }

	private final Map<Integer, Boolean> entries = new HashMap<>();
	private final Consumer<Boolean> toggle;

	/**
	 * Constructor.
	 * @param toggle   Toggle
	 * @param times    List of open/closing times
	 * @throws IllegalArgumentException if the list of times is empty or is not in increasing order
	 */
	public ToggleListener(Consumer<Boolean> toggle, List<Entry> times) {
        this.toggle = notNull(toggle);
	    Check.notEmpty(times);
	    int prev = -1;
	    for(Entry entry : times) {
	        // Validate entry
	        if(entry.close <= entry.open) throw new IllegalArgumentException("Cannot close before opening!");
	        if(entry.open <= prev) throw new IllegalArgumentException("Hours must be ascending");
	        prev = entry.close;

	        // Add entry
            entries.put(entry.open, true);
            entries.put(entry.close, false);
	    }
	}

	@Override
	public void update(int hour) {
	    final Boolean open = entries.get(hour);
	    if(open != null) {
	        toggle.accept(open);
	    }
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
