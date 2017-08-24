package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.DamageType;
import org.sarge.textrpg.entity.DamageEffect;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;

/**
 * Action to damage an object.
 * @author Sarge
 */
public class DamageObjectAction extends AbstractAction {
	@Override
	public Stance[] getInvalidStances() {
		return new Stance[]{Stance.RESTING};
	}

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
		final Weapon weapon = (Weapon) actor.getWeapon().getDescriptor();
		final DamageEffect damage = weapon.getDamage();
		final DamageType type = damage.getDamageType();
		if(!obj.getDescriptor().getCharacteristics().getMaterial().isDamagedBy(type)) {
			throw new ActionException("damage.object.invalid");
		}
		
		// Damage object
		obj.damage(damage.getDamageType(), damage.getAmount().evaluate(actor));
		
		// Build response
		return new ActionResponse("damage.object." + (obj.isDead() ? "smashed" : "ok"));
	}
}
