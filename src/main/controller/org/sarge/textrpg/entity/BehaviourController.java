package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.FleeAction;
import org.sarge.textrpg.world.MovementController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * Default AI behaviour controller.
 * @author Sarge
 */
@Controller
public class BehaviourController {
	private static final Logger LOG = LoggerFactory.getLogger(BehaviourController.class);

	private final FleeAction flee;
	private final MovementController controller;

	/**
	 * Constructor.
	 * @param controller		Movement controller
	 * @param flee 				Flee action
	 */
	public BehaviourController(MovementController controller, FleeAction flee) {
		this.controller = notNull(controller);
		this.flee = notNull(flee);
	}

	/**
	 * Reverts to the default behaviour of the given entity.
	 * @param actor Actor
	 */
	public void revert(Entity actor) {
		final Race.Behaviour behaviour = actor.descriptor().race().behaviour();
		if(!behaviour.isIdle()) {
			start(actor, behaviour);
		}
	}

	/**
	 * Starts the default behaviour induction.
	 * @param actor			Actor
	 * @param behaviour		Default behaviour descriptor
	 */
	private void start(Entity actor, Race.Behaviour behaviour) {
		// TODO - refactor using follower?
		// Create movement induction
		final Induction induction = () -> {
			// Select next exit
			final Optional<Exit> exit = behaviour.movement().next(actor.location());
			if(!exit.isPresent()) {
				// TODO - flag to indicate entity is destroyed?
				LOG.debug("No available exits for entity: actor={} loc={}", actor, actor.location());
				return Response.EMPTY;
			}

			// Traverse exit
			try {
				controller.move(actor, exit.get(), 1);
			}
			catch(ActionException e) {
				LOG.error("Entity failed to traverse exit: actor={} exit={}", actor, exit);
			}
			return Response.EMPTY;

			// TODO
			// - check startled
		};

		// Start induction
		final Induction.Descriptor descriptor = new Induction.Descriptor.Builder().period(behaviour.period()).flag(Induction.Flag.REPEATING).build();
		final Induction.Instance instance = new Induction.Instance(descriptor, induction);
		actor.manager().induction().start(instance);
	}

	/**
	 * Actor flees.
	 * @param actor Actor Fleeing entity
	 */
	public void flee(Entity actor) {
		execute(actor, flee.flee(actor));
	}

	/**
	 * Actor attacks the given target.
	 * @param actor			Actor
	 * @param target		Entity to attack
	 */
	public void attack(Entity actor, Entity target) {
		// TODO - initiate combat
	}

	/**
	 * Executes an action.
	 * @param response
	 */
	private void execute(Entity actor, Response response) {
		final var induction = response.induction();
		if(induction.isPresent()) {
			// Register action induction
			actor.manager().induction().start(induction.get());
		}
		else {
			// Otherwise revert to default behaviour
			revert(actor);
		}
	}
}
