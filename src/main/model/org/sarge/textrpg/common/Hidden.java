package org.sarge.textrpg.common;

import org.sarge.textrpg.util.Percentile;

/**
 * Defines a partially hidden object or entity.
 * @author Sarge
 */
@FunctionalInterface
public interface Hidden {
	/**
	 * @return Visibility level of this partially hidden object
	 */
	Percentile visibility();
}
