package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.Randomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Swimming controller.
 * @author Sarge
 */
@Component
public class SwimmingController {
	private static final Response DROWNING = Response.of("swim.drowning");

	private final Skill skill;
	private final EffectController controller;

	private Duration period = Duration.ofSeconds(1);
	private int cost = 1;
	private int damage = 1;
	private int panic = 1;

	/**
	 * Constructor.
	 * @param controller		Controller for panic effect
	 * @param skill 			Swimming skill
	 */
	@Autowired
	public SwimmingController(EffectController controller, @Value("#{skills.get('swim')}") Skill skill) {
		this.controller = notNull(controller);
		this.skill = notNull(skill);
	}

	/**
	 * Sets the iteration period.
	 * @param period Iteration period
	 */
	@Autowired
	public void setIterationPeriod(@Value("${swim.iteration.period}") Duration period) {
		this.period = notNull(period);
	}

	/**
	 * Sets the stamina cost per iteration.
	 * @param cost Stamina cost
	 */
	@Autowired
	public void setStaminaCost(@Value("${swim.stamina.cost}") int cost) {
		this.cost = oneOrMore(cost);
	}

	/**
	 * Sets the damage sustained when drowning.
	 * @param damage Damage amount
	 */
	@Autowired
	public void setDrowningDamage(@Value("${swim.drown.damage}") int damage) {
		this.damage = oneOrMore(damage);
	}

	/**
	 * Sets the amount of panic sustained when drowning.
	 * @param panic Panic amount
	 */
	@Autowired
	public void setDrowningPanic(@Value("${swim.drown.panic}") int panic) {
		this.panic = oneOrMore(panic);
	}

	/**
	 * Starts swimming.
	 * @param actor Actor
	 * @throws IllegalStateException if the actor is already swimming or cannot swim
	 * @see Entity#isSwimEnabled()
	 */
	public void start(Entity actor) {
		if(!actor.isSwimEnabled()) throw new IllegalStateException("Actor cannot swim: " + actor);

		// Check stance
		switch(actor.model().stance()) {
		case DEFAULT:
			break;

		case SNEAKING:
			actor.model().values().visibility().remove();
			break;

		default:
			throw new IllegalStateException("Invalid stance: " + actor);
		}

		// Start swimming
		final Induction.Descriptor descriptor = new Induction.Descriptor.Builder()
			.period(period)
			.flag(Induction.Flag.PRIMARY)
			.flag(Induction.Flag.REPEATING)
			.build();
		actor.model().stance(Stance.SWIMMING);
		actor.manager().induction().start(new Induction.Instance(descriptor, () -> update(actor)));
	}

	/**
	 * Stops swimming.
	 * @param actor Actor
	 * @throws IllegalStateException if not swimming
	 */
	public void stop(Entity actor) {
		if(actor.model().stance() != Stance.SWIMMING) throw new IllegalStateException("Not swimming: " + actor);
		actor.manager().induction().stop();
		actor.model().stance(Stance.DEFAULT);
	}

	/**
	 * Swimming iteration.
	 * @param actor Actor
	 * @return Response
	 */
	Response update(Entity actor) {
		// Check swimming skill
		int count = 0;
		final Percentile score = actor.skills().find(skill).score();
		if(!Randomiser.isLessThan(score)) {
			++count;
		}

		// Consume stamina
		final var values = actor.model().values();
		final Transaction tx = values.transaction(EntityValue.STAMINA, cost, "swim.insufficient.stamina");
		if(tx.isValid()) {
			tx.complete();
		}
		else {
			++count;
		}

		// Drown if either fail
		if(count > 0) {
			values.get(EntityValue.HEALTH.key()).modify(-damage * count);
			controller.panic(actor, panic);
		}

		// Build response
		if(count > 0) {
			return DROWNING;
		}
		else {
			return Response.EMPTY;
		}
	}
}
