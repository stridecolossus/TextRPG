package org.sarge.textrpg.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that an object argument <b>must</b> be carried by the actor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Carried {
	/**
	 * @return Whether the object can be automatically picked up before this action
	 */
	boolean auto() default false;
}
