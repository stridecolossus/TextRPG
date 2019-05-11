package org.sarge.textrpg.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Specifies that an action is parameterised by the given enumeration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface EnumAction {
	/**
	 * @return Enumeration class
	 */
	Class<? extends Enum<?>> value();
}
