package org.sarge.textrpg.common;

import java.util.stream.Stream;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.contents.Parent;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.util.Description;

/**
 * An <i>actor</i> is an entity that can perform actions in the world.
 * @author Sarge
 */
public interface Actor extends Parent {
	/**
	 * @param hidden Partially hidden object
	 * @return Whether this actor can perceive the given hidden object
	 */
	boolean perceives(Hidden hidden);

	/**
	 * Notifies this actor of an event.
	 * @param alert Alert description
	 */
	void alert(Description alert);

	/**
	 * @param cat Category
	 * @return Whether this actor is a member of the given race category
	 */
	boolean isRaceCategory(String cat);

	/**
	 * @return Skills possessed by this actor
	 */
	SkillSet skills();

	/**
	 * Broadcasts a notification to a set of actors (except the originator).
	 * @param actor			Optional originating actor
	 * @param alert			Alert description
	 * @param stream		Actors
	 */
	static void broadcast(Actor actor, Description alert, Stream<? extends Thing> stream) {
		StreamUtil.select(Actor.class, stream)
			.filter(e -> e != actor)
			.forEach(e -> e.alert(alert));
	}
}
