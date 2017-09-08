package org.sarge.textrpg.common;

/**
 * Parent/container of a {@link Thing}.
 * @author Sarge
 */
public interface Parent {
	/**
	 * @return Contents
	 */
	Contents contents();
	
	/**
	 * @return Parent of this object or <tt>null</tt> if none
	 */
	Parent parent();

	/**
	 * @return Parent identifier
	 * @throws UnsupportedOperationException by default
	 */
	default String parentName() {
		throw new UnsupportedOperationException();
	}
}
