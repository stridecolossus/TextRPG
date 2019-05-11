package org.sarge.textrpg.common;

/**
 * A <i>command argument</i> is a candidate action argument.
 * @author Sarge
 */
@FunctionalInterface
public interface CommandArgument {
	/**
	 * @return Argument identifier
	 */
	String name();
}
