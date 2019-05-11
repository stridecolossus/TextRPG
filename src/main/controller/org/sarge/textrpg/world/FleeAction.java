package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;
import java.util.List;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.EffectController;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Randomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Flees the current location in a random direction.
 * @author Sarge
 */
@Component
public class FleeAction extends AbstractAction {
	private final EffectController controller;
	private final MovementController mover;

	private int mod = 1;
	private int panic = 1;
	private Duration delay = Duration.ofMinutes(1);

	/**
	 * Constructor.
	 * @param mover 			Movement controller
	 * @param controller		Effects controller
	 */
	public FleeAction(MovementController mover, EffectController controller) {
		super(Flag.REVEALS, Flag.OUTSIDE);
		this.mover = notNull(mover);
		this.controller = notNull(controller);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
	}

	/**
	 * Sets the movement cost modifier when fleeing.
	 * @param mod Movement cost modifier
	 */
	@Autowired
	public void setMovementCostModifier(@Value("${flee.cost.modifier}") int mod) {
		this.mod = oneOrMore(mod);
	}

	/**
	 * Sets the delay when fleeing in combat.
	 * @param delay Delay
	 */
	@Autowired
	public void setFleeDelay(@Value("${flee.delay}") Duration delay) {
		this.delay = notNull(delay);
	}

	/**
	 * Sets the amount of panic when fleeing fails.
	 * @param panic Panic level
	 */
	@Autowired
	public void setPanic(@Value("${flee.panic}") int panic) {
		this.panic = oneOrMore(panic);
	}

	/**
	 * Flees the current location.
	 * @param actor Feeling actor
	 * @return Response
	 */
	@RequiresActor
	public Response flee(Entity actor) {
		// Interrupt any active induction
		interrupt(actor);

		// Flee
		if(actor.manager().induction().isPrimary()) {
			// Queue flee attempt
			final Induction.Descriptor flee = new Induction.Descriptor.Builder().period(delay).flag(Induction.Flag.REPEATING).build();
			return Response.of(new Induction.Instance(flee, () -> execute(actor)));
		}
		else {
			// Flee immediately
			return execute(actor);
		}
	}

	/**
	 * Flees the current location.
	 * @param actor Fleeing actor
	 * @return Response
	 */
	private Response execute(Entity actor) {
		// Enumerate available exits
		final List<Exit> exits = actor.location().exits().stream()
			.filter(exit -> exit.link().isTraversable())
			.filter(exit -> exit.isPerceivedBy(actor))
			.collect(toList());

		// Check for available exit
		if(exits.isEmpty()) {
			controller.panic(actor, panic);
			return Response.of("flee.cannot.flee");
		}

		// Flee in random direction
		final Exit exit = Randomiser.select(exits);
		try {
			mover.move(actor, exit, mod);
		}
		catch(ActionException e) {
			controller.panic(actor, panic);
			return Response.of(e.description());
		}

		// Apply panic effect
		controller.panic(actor, 1);

		// Display location
		return new Response.Builder().add(Description.of("action.flee.message")).display().build();
	}
}
