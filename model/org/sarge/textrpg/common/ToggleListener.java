package org.sarge.textrpg.common;

import java.util.function.Consumer;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Clock listener to toggle the state of something such as shop opening times.
 * @author Sarge
 */
public class ToggleListener implements Clock.Listener {
	private final int[] hours;
	private final Consumer<Boolean> toggle;

	/**
	 * Constructor.
	 * @param open		Open time
	 * @param toggle	Toggle
	 * @throws IllegalArgumentException if the array of hours is not <i>even</i> and in ascending order
	 */
	public ToggleListener(Consumer<Boolean> toggle, int[] hours) {
		if(hours.length == 0) throw new IllegalArgumentException("Empty list of hours");
		if((hours.length % 2) == 1) throw new IllegalArgumentException("Number of hours must be even");
		int prev = -1;
		for(int hour : hours) {
			Check.range(hour, 0, 23);
			if(hour <= prev) throw new IllegalArgumentException("Hours must be ascending");
			prev = hour;
		}
		Check.notNull(toggle);
		this.hours = hours.clone();
		this.toggle = toggle;
	}
	
	@Override
	public void update(int hour) {
		for(int n = 0; n < hours.length; ++n) {
			if(hours[n] == hour) {
				final boolean open = (n % 2) == 0;
				toggle.accept(open);
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
