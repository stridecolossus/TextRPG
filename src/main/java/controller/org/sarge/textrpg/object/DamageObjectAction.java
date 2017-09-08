package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractActiveAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.entity.DamageEffect;
import org.sarge.textrpg.entity.Entity;

/**
 * Action to damage an object.
 * @author Sarge
 */
public class DamageObjectAction extends AbstractActiveAction {
	/**
	 * Damage an object.
	 * @param ctx
	 * @param actor
	 * @param obj
	 * @return
	 * @throws ActionException
	 */
	public ActionResponse smash(Entity actor, WorldObject obj) throws ActionException {
		// Check can damage this object
		final Weapon weapon = (Weapon) actor.weapon().descriptor();
		final DamageEffect damage = weapon.damage();
		final DamageType type = damage.damageType();
		if(!obj.descriptor().getCharacteristics().getMaterial().isDamagedBy(type)) {
			throw new ActionException("damage.object.invalid");
		}

		// Damage object
		obj.damage(damage.damageType(), damage.amount().evaluate(actor));

		// Build response
		return new ActionResponse("damage.object." + (obj.isDead() ? "smashed" : "ok"));
	}
}
