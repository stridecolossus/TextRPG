package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * Combat notification.
 * @author Sarge
 */
public final class CombatNotification extends Notification {
	/**
	 * Combat notifications.
	 */
	public enum Type {
		/**
		 * Combat initiated.
		 */
		ATTACKED,

		/**
		 * Entity has sustained damage.
		 */
		DAMAGED,

		/**
		 * Entity killed in combat.
		 */
		KILLED;

		private final String key = TextHelper.join("notification.combat", this.name());
	}

	private final Type type;
	private final Damage.Type damage;
	private final int amount;

	/**
	 * Constructor.
	 * @param type			Notification sub-type
	 * @param damage		Type of damage
	 * @param amount		Amount of type
	 */
	public CombatNotification(Entity entity, Type type, Damage.Type damage, int amount) {
		super(type.key, entity);
		this.type = notNull(type);
		this.damage = notNull(damage);
		this.amount = notNull(amount);
	}

	@Override
	public void handle(Handler handler, Entity e) {
		handler.handle(this, e);
	}

	/**
	 * @return Notification sub-type
	 */
	public Type type() {
		return type;
	}

	@Override
	protected void describe(Description.Builder builder) {
		builder.add("damage", damage);
		builder.add("amount", amount, ArgumentFormatter.PLAIN);
	}
}
