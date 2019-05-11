package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.entity.Transaction;
import org.sarge.textrpg.util.ActionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to whet a weapon.
 * @author Sarge
 * @see Damage.Type#isWhetWeapon()
 */
@Component
@RequiresActor
public class WhetAction extends AbstractAction {
	private final Duration duration;

	/**
	 * Constructor.
	 * @param duration Whet iteration period
	 */
	public WhetAction(@Value("${whet.period}") Duration duration) {
		super(Flag.INDUCTION, Flag.BROADCAST);
		this.duration = notNull(duration);
	}

	@Override
	protected boolean isValid(Stance stance) {
		if(stance == Stance.RESTING) {
			return true;
		}
		else {
			return super.isValid(stance);
		}
	}

	/**
	 * Whets the currently equipped weapon.
	 * @param weapon Weapon to whet
	 * @return Response
	 * @throws ActionException if the weapon cannot be whetted or is not damaged
	 */
	@RequiredObject("whetstone")
	public Response whet(Entity actor) throws ActionException {
		final Weapon weapon = actor.contents().equipment().weapon().orElseThrow(() -> ActionException.of("whet.requires.weapon"));
		return whet(actor, weapon);
	}

	/**
	 * Whets the given weapon.
	 * @param actor		Actor
	 * @param weapon	Weapon to whet
	 * @return Response
	 * @throws ActionException if the weapon cannot be whetted or is not damaged
	 */
	@RequiredObject("whetstone")
	public Response whet(Entity actor, @Carried Weapon weapon) throws ActionException {
		// Check can be whetted
		if(!weapon.descriptor().damage().type().isWhetWeapon()) throw ActionException.of("whet.invalid.weapon");
		if(!weapon.isDamaged()) throw ActionException.of("whet.not.damaged");

		// Create stamina modifier
		// TODO - tx can only be used once!
		final Transaction transaction = actor.model().values().transaction(EntityValue.STAMINA, 1, "whet.exhausted");
		transaction.check();

		// Create repeating whet induction
		final Induction induction = () -> {
			// Consume stamina
			transaction.check();
			transaction.complete();

			// Stop when fully whetted
			weapon.repair(1);
			if(!weapon.isDamaged()) throw ActionException.of("whet.finished");

			return Response.EMPTY;
		};

		// Build response
		final Induction.Descriptor descriptor = new Induction.Descriptor.Builder()
			.period(duration)
			.flag(Induction.Flag.SPINNER)
			.flag(Induction.Flag.REPEATING)
			.build();
		return Response.of(new Induction.Instance(descriptor, induction));
	}
}
