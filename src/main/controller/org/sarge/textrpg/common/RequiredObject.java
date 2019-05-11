package org.sarge.textrpg.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the category for an object required by an action.
 */
@Repeatable(RequiredObject.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface RequiredObject {
	/**
	 * @return Object category
	 */
	String value();

	// Repeatable declaration for multiple required objects on a method
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	@interface List {
		RequiredObject[] value();
	}
}
