package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.Description;

/**
 * Base-class for a notification.
 * @author Sarge
 */
public abstract class Notification extends AbstractEqualsObject {
	/**
	 * Handler for entity notifications.
	 */
	public interface Handler {
		/**
		 * Initialises the default behaviour for the given entity.
		 */
		void init(Entity actor);

		/**
		 * Handles a movement notification.
		 * @param move 		Notification
		 * @param actor		Actor
		 */
		void handle(MovementNotification move, Entity actor);

		/**
		 * Handles an emission notification.
		 * @param emission 	Notification
		 * @param actor		Actor
		 */
		void handle(EmissionNotification emission, Entity actor);

		/**
		 * Handles a combat notification.
		 * @param combat 	Notification
		 * @param actor		Actor
		 */
		void handle(CombatNotification combat, Entity actor);

		/**
		 * Handles a light activation event.
		 * @param actor Actor
		 */
		void light(Entity actor);
	}

	private final String key;
	private final Entity actor;

	private transient Description description;

	/**
	 * Constructor.
	 * @param key 		Description key
	 * @param actor		Optional actor that generated this notification
	 */
	protected Notification(String key, Entity actor) {
		this.key = notEmpty(key);
		this.actor = actor;
	}

	/**
	 * @return Description key
	 */
	public final String key() {
		return key;
	}

	/**
	 * @return Actor that generated this notification or <tt>null</tt> if anonymous
	 */
	public final Entity actor() {
		return actor;
	}

	/**
	 * Describes this notification.
	 * @return Description
	 */
	public final Description describe() {
		if(description == null) {
			final var builder = new Description.Builder(key);
			if(actor != null) {
				builder.name(actor.name());
			}
			describe(builder);
			description = builder.build();
		}

		return description;
	}

	/**
	 * Adds description arguments.
	 * @param builder Description builder
	 */
	protected abstract void describe(Description.Builder builder);

	/**
	 * Double-dispatches this notification to the given entity.
	 * @param entity Entity
	 */
	public abstract void handle(Handler handler, Entity entity);
}
