package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.Transaction;
import org.sarge.textrpg.util.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Action to dig snow in an exit.
 * @author Sarge
 */
@Component
public class DigAction extends AbstractAction {
	private final SnowModel model;

	private Duration period;
	private int cost;
	private int lifetime;

	/**
	 * Constructor.
	 * @param model Snow level model
	 */
	public DigAction(SnowModel model) {
		super(Flag.LIGHT, Flag.REVEALS, Flag.INDUCTION);
		this.model = notNull(model);
	}

	/**
	 * Sets the dig iteration period.
	 * @param period Iteration period
	 */
	@Autowired
	public void setIterationPeriod(@Value("${dig.snow.period}") Duration period) {
		this.period = notNull(period);
	}

	/**
	 * Sets the stamina cost per iteration.
	 * @param cost Stamina cost
	 */
	@Autowired
	public void setStaminaCost(@Value("${dig.snow.stamina.cost}") int cost) {
		this.cost = oneOrMore(cost);
	}

	/**
	 * Sets the lifetime of removed snow.
	 * @param lifetime Lifetime
	 */
	@Autowired
	public void setLifetime(@Value("${dig.snow.lifetime}") int lifetime) {
		this.lifetime = oneOrMore(lifetime);
	}

	/**
	 * Digs snow in the given exit direction.
	 * @param actor		Actor
	 * @param dir		Direction
	 * @return Response
	 * @throws ActionException if there is no exit in the given direction or the destination location has no snow to dig
	 */
	@RequiresActor
	@RequiredObject("shovel")
	public Response dig(Entity actor, Direction dir) throws ActionException {
		// Lookup snow level at the destination
		final Exit exit = actor.location().exits().find(dir).orElseThrow(() -> ActionException.of("dig.snow.invalid"));
		final int snow = model.snow(exit.destination());
		if(snow == 0) throw ActionException.of("dig.snow.none");

		// Create dig induction
		final Induction induction = () -> {
			// Consume stamina
			final Transaction tx = actor.model().values().transaction(EntityValue.STAMINA, cost, "dig.snow.exhausted");
			tx.check();
			tx.complete();

			// Dig snow
			final int amount = Math.min(1, actor.model().attributes().get(Attribute.STRENGTH).get());
			final int level = model.reduce(exit.destination(), amount, lifetime);

			// Stop if finished
			if(level == 0) {
				throw ActionException.of("dig.snow.finished");
			}

			return Response.EMPTY;
		};

		// Build response
		return Response.of(new Induction.Instance(induction, period));
	}
}
