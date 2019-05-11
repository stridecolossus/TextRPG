package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;

/**
 * An <i>action exception</i> represents an attempt by an entity to perform an invalid or illogical action.
 * @author Sarge
 */
public class ActionException extends Exception {
	private static final Map<String, ActionException> CACHE = new HashMap<>();

	/**
	 * Creates a cached action exception.
	 * @param message Exception message
	 * @return Action exception
	 * TODO - should this actually THROW the exception? avoids bug of client calling of() without actually throwing!
	 */
	public static ActionException of(String message) {
		return CACHE.computeIfAbsent(message, ActionException::new);
	}

	/**
	 * Creates a cached action exception.
	 * @param message Message argument(s)
	 * @return Action exception
	 */
	public static ActionException of(Object... message) {
		return of(TextHelper.join(message));
	}

	private final Description description;

	/**
	 * Constructor.
	 * @param description Description of this exception
	 */
	public ActionException(Description description) {
		super(description.key());
		this.description = notNull(description);
	}

	/**
	 * Convenience constructor.
	 * @param key Description key
	 */
	private ActionException(String key) {
		this(Description.of(key));
	}

	/**
	 * @return Description of this exception
	 */
	public Description description() {
		return description;
	}
}
