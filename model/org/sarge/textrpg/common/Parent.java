package org.sarge.textrpg.common;

/**
 * Parent/container of a {@link Thing}.
 * @author Sarge
 */
public interface Parent {
	/**
	 * @return Contents
	 */
	Contents getContents();
	
	/**
	 * @return Parent of this object or <tt>null</tt> if none
	 */
	Parent getParent();

	/**
	 * @return Parent identifier
	 * @throws UnsupportedOperationException by default
	 */
	default String getParentName() {
		throw new UnsupportedOperationException();
	}
}
