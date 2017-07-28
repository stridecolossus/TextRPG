package org.sarge.textrpg.runner;

import java.util.Arrays;
import java.util.stream.Stream;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Thing;

/**
 * Command argument builder.
 * @author Sarge
 */
public interface ArgumentBuilder {
	/**
	 * @return Arguments
	 */
	Stream<?> stream(Actor actor);

	/**
	 * Creates an argument matcher based on an enumeration.
	 * @param clazz
	 * @return Enumeration argument matcher
	 */
	static <E extends Enum<E>> ArgumentBuilder enumeration(Class<E> clazz) {
		return actor -> Arrays.stream(clazz.getEnumConstants());
	}

	/**
	 * Object argument matcher.
	 * @param str String
	 * @return String argument matcher
	 */
	static ArgumentBuilder object(Object obj) {
		return actor -> Stream.of(obj);
	}

	/**
	 * Contents argument matcher.
	 * @param contents Contents
	 * @return Contents argument matcher
	 */
	static ArgumentBuilder of(Contents contents) {
		return actor -> contents.stream()
			.filter(StreamUtil.not(Thing::isDead))
			.filter(actor::perceives)
			.map(t -> t);
	}
}
