package org.sarge.textrpg.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that an action class or method supports an optional {@link AbstractAction.Effort} argument.
 * @author Sarge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface EffortAction {
	// Marker
}
