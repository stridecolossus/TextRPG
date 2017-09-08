package org.sarge.textrpg.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Liquid;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Receptacle;
import org.sarge.textrpg.object.WorldObject;

/**
 * Contents helper methods.
 * @author Sarge
 */
public final class ContentsHelper {
	private ContentsHelper() {
		// Utility class
	}
	
	/**
	 * Contents filter for objects and entities.
	 */
	private static final Predicate<Thing> FILTER = t -> {
		if(t instanceof WorldObject) return true;
		if(t instanceof Entity) return true;
		return false;
	};

	/**
	 * Creates a filter to only return objects and entities that are visible to the given actor.
	 * @param actor Actor
	 * @return Filter
	 */
	public static Predicate<Thing> filter(Actor actor) {
		return FILTER.and(Actor.filter(actor)).and(actor::perceives);
	}
	
	/**
	 * Helper - Creates a matcher for objects of the given descriptor.
	 * @param descriptor Object descriptor to match
	 * @return Matcher
	 */
	public static final Predicate<WorldObject> objectMatcher(ObjectDescriptor descriptor) {
		return obj -> obj.descriptor() == descriptor;
	}

	/**
	 * Helper - Creates a matcher for receptacles containing the given liquid.
	 * @param liquid Liquid to match
	 * @return Matcher
	 */
	public static final Predicate<WorldObject> receptacleMatcher(Liquid liquid) {
		return obj -> {
			if(obj instanceof Receptacle) {
				final Receptacle rec = (Receptacle) obj;
				return rec.descriptor().liquid() == liquid;
			}
			else {
				return false;
			}
		};
	}

	/**
	 * Helper - Creates a matcher for object categories.
	 * @param cats Categories
	 * @return Category matcher
	 */
	public static final Predicate<WorldObject> categoryMatcher(String... cats) {
		final Collection<String> set = Arrays.asList(cats);
		return obj -> obj.descriptor().getCharacteristics().getCategories().anyMatch(set::contains);
	}

	/**
	 * Helper - Filters and maps objects of the specified class.
	 * @param stream Contents stream
	 * @return Stream of objects
	 */
	public static final <T> Stream<T> select(Stream<Thing> stream, Class<T> clazz) {
		return stream
			.filter(t -> clazz.isAssignableFrom(t.getClass()))
			.map(t -> clazz.cast(t));
	}
}
