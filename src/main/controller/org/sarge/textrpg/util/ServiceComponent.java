package org.sarge.textrpg.util;

/**
 * Defines a component that is started after the application has successfully loaded.
 * <p>
 * The intention of this mechanism is to:
 * <ul>
 * <li>prevent thread-based components from being initialised before the application is ready</li>
 * <li>ensure errors during bean initialisation and/or data loading cause the application to fail (rather than partially running)</li>
 * <li>minimise coupling to the DI framework</li>
 * </ul>
 * <p>
 * @author Sarge
 */
@FunctionalInterface
public interface ServiceComponent {
	/**
	 * Starts this component.
	 */
	void start();
}
