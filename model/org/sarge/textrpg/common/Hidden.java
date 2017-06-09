package org.sarge.textrpg.common;

import org.sarge.textrpg.util.Percentile;

/**
 * Defines something that is partially visible.
 * @author Sarge
 */
public interface Hidden {
	/**
	 * @return Visibility of this object
	 */
	Percentile getVisibility();
	
	/**
	 * @return Duration after which this object is forgotten (ms)
	 */
	long getForgetPeriod();
}
