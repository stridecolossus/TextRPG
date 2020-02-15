package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.entity.PlayerSettings;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter.PlainArgument;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Randomiser;
import org.sarge.textrpg.util.TextHelper;
import org.springframework.stereotype.Component;

/**
 * Action to roll a dice or flip a coin.
 * @author Sarge
 */
@Component
public class DiceAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public DiceAction() {
		super(Flag.OUTSIDE, Flag.BROADCAST);
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
	 * Rolls a six-sided dice.
	 * @return Response
	 */
	@RequiredObject("dice")
	public Response roll() {
		final int side = 1 + Randomiser.range(6);
		final Description response = new Description.Builder("action.roll.dice").add("side", new PlainArgument(side)).build();
		return Response.of(response);
	}

	/**
	 * Flips a coin.
	 * @param actor Actor
	 * @return Response
	 * @throws ActionException if the actor does not have any coins
	 */
	@RequiresActor
	public Response flip(PlayerCharacter actor) throws ActionException {
		if(actor.settings().toInteger(PlayerSettings.Setting.CASH) == 0) throw ActionException.of("flip.requires.coin");
		final boolean heads = Randomiser.RANDOM.nextBoolean();
		final String side = TextHelper.join("coin", heads ? "heads" : "tails");
		final Description response = new Description.Builder("action.flip.coin").add("side", side).build();
		return Response.of(response);
	}
}
