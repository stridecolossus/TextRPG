package org.sarge.textrpg.object;

import java.util.Optional;
import java.util.function.Predicate;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Equipment;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Sheaths or draws weapons.
 * @author Sarge
 */
@Component
@RequiresActor
public class SheathAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public SheathAction() {
		super(Flag.OUTSIDE, Flag.BROADCAST);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	/**
	 * Sheaths the currently equipped weapon in an available sheath.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor is not wielding a weapon or does not have an appropriate sheath equipped
	 */
	public Response sheath(Entity actor) throws ActionException {
		final Sheath sheath = actor.contents().equipment().select(Sheath.class).findAny().orElseThrow(() -> ActionException.of("sheath.not.found"));
		return sheath(actor, sheath);
	}

	/**
	 * Sheaths the currently equipped weapon in the given sheath.
	 * @param actor			Actor
	 * @param sheath		Weapon sheath
	 * @return Response
	 * @throws ActionException if the actor is not wielding a weapon, or the given sheath is not equipped or cannot be used
	 */
	public Response sheath(Entity actor, @Carried Sheath sheath) throws ActionException {
		// Check sheath is equipped
		final Equipment equipment = actor.contents().equipment();
		if(!equipment.contains(sheath)) throw ActionException.of("sheath.not.equipped");

		// Check wielding a weapon
		final Weapon weapon = equipment.weapon().orElseThrow(() -> ActionException.of("sheath.requires.weapon"));

		// Sheath weapon
		sheath.sheath(weapon);
		equipment.remove(Slot.MAIN);

		// Build response
		return Response.OK;
	}

	/**
	 * Draws a weapon from an equipped sheath.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if there is no sheathed weapon
	 */
	public Response draw(Entity actor) throws ActionException {
		final Predicate<Sheath> matcher = sheath -> sheath.weapon().isPresent();
		return draw(actor, matcher, "draw.requires.weapon");
	}

	/**
	 * Draws the given weapon.
	 * @param actor			Actor
	 * @param weapon		Weapon to draw
	 * @return Response
	 * @throws ActionException if the weapon is not sheathed
	 */
	public Response draw(Entity actor, Weapon weapon) throws ActionException {
		final Predicate<Sheath> matcher = sheath -> sheath.weapon().filter(obj -> obj == weapon).isPresent();
		return draw(actor, matcher, "draw.not.sheathed");
	}

	/**
	 * Draws a sheathed weapon.
	 * @param actor			Actor
	 * @param filter		Sheath filter
	 * @param message		Message if no matching sheath
	 * @return Response
	 * @throws ActionException if no matching sheath is equipped
	 */
	private Response draw(Entity actor, Predicate<Sheath> matcher, String message) throws ActionException {
		// Sheath current weapon (if any)
		final Equipment equipment = actor.contents().equipment();
		final Optional<Weapon> current = equipment.weapon();
		// TODO - how to swap?

		// Find a sheathed weapon
		final Sheath sheath = equipment.select(Sheath.class)
			// TODO - filter out current weapon
			.filter(matcher)
			.findAny()
			.orElseThrow(() -> ActionException.of(message));

		// Wield weapon
		final Weapon weapon = sheath.weapon().get();
		sheath.clear();

		// Equip weapon and build response
		equipment.equip(weapon, Slot.MAIN);

		// Build response
		return Response.OK;
	}
}
