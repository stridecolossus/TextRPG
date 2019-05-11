package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;

import org.sarge.lib.collection.InverseMap.InverseHashMap;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.Event;
import org.springframework.beans.factory.annotation.Value;

/**
 * Action for a {@link Riddle} contest.
 * @author Sarge
 * TODO - watch <entity> to watch a riddle contest
 */
public class RiddleAction extends AbstractAction {
	private static final Description RESIGNED	= Description.of("riddle.contest.resigned");
	private static final Description ACCEPTED	= Description.of("riddle.contest.accepted");
	private static final Description WON		= Description.of("riddle.contest.won");
	private static final Description LOST		= Description.of("riddle.contest.lost");
	private static final Description DRAWN		= Description.of("riddle.contest.drawn");

	private final InverseHashMap<Entity, Riddle.Model> active = new InverseHashMap<>();

	private final int def, max;
	private final Duration limit;

	/**
	 * Constructor.
	 * @param def		Default number of rounds
	 * @param max		Maximum number of rounds
	 * @param limit		Time limit
	 */
	public RiddleAction(@Value("riddle.default.rounds") int def, @Value("riddle.max.rounds") int max, @Value("riddle.timeout") Duration limit) {
		super(Flag.INDUCTION);
		this.def = oneOrMore(def);
		this.max = oneOrMore(max);
		this.limit = notNull(limit);
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		// TODO
		// - riddles known by actor (and other player?)
		// - answers
		// - how to handle incorrect answer?
		return super.parsers(actor);
	}

	// add guessed riddles

	/**
	 * Starts a riddle contest with the default number of rounds.
	 * @param actor			Challenger
	 * @param entity		Challenged entity
	 * @throws ActionException if either entity cannot participate
	 */
	public void start(Entity actor, Entity entity) throws ActionException {
		start(actor, entity, def);
	}

	/**
	 * Starts a riddle contest.
	 * @param actor			Challenger
	 * @param entity		Challenged entity
	 * @param rounds		Number of rounds
	 * @throws ActionException if either entity cannot participate
	 */
	public void start(Entity actor, Entity entity, Integer rounds) throws ActionException {
		// Check contest can be created
		if(rounds > max) throw ActionException.of("riddle.invalid.rounds");
		if(active.containsKey(actor)) throw ActionException.of("riddle.actor.already");
		if(active.containsKey(entity)) throw ActionException.of("riddle.entity.already");

		// Create contest model
		final var model = new Riddle.Model(actor, entity, rounds);
		active.put(actor, model);
		active.put(entity, model);

		// Notify challenged entity
		entity.alert(new Description("riddle.challenge", actor.name()));

		// Start inductions
		start(actor, model);
		start(entity, model);

		// Register timeout
		register(model);
	}

	/**
	 * Starts a riddle induction.
	 * @param entity		Entity
	 * @param model			Riddle model
	 */
	private void start(Entity entity, Riddle.Model model) {
		// Create interrupt handler
		final Runnable interrupt = () -> {
			model.interrupt();
			alert(model, Description.of("riddle.contest.interrupted"));
			cleanup(model);
		};

		// Start riddle contest
// TODO
//		final var induction = Induction.indefinite(interrupt);
//		entity.stack().push(induction);
	}

	/**
	 * Register the contest timeout.
	 * @param model
	 */
	private void register(Riddle.Model model) {
		final Event timeout = () -> {
			model.interrupt();
			alert(model, Description.of("riddle.contest.interrupted"));
			// TODO - alert other
			cleanup(model);
			return false;
		};
// TODO
//		final Event.Reference ref = new Event.Entry(timeout, limit.toMillis());
//		model.holder().set(entry);
	}

	/**
	 * Finds the active model for the given actor.
	 * @param actor Actor
	 * @return Riddle model
	 * @throws ActionException if not playing
	 */
	private Riddle.Model find(Entity actor) throws ActionException {
		final var model = active.get(actor);
		if(model == null) throw ActionException.of("riddle.not.active");
		if(actor != model.who()) throw ActionException.of("riddle.not.turn");
		return model;
	}

	/**
	 * Accepts or declines a challenger.
	 * @param actor			Actor
	 * @param accept		Whether accepted
	 * @throws ActionException if not challenged
	 */
	public Response accept(Entity actor, boolean accept) throws ActionException {
		// Update model
		final var model = find(actor);
		if(accept) {
			model.accept();
			register(model);
		}
		else {
			alert(model, Description.of("riddle.contest.declined"));
			cleanup(model);
		}

		// Notify challenger
		alert(model, ACCEPTED);

		// Build response
		return Response.of(ACCEPTED);
	}

	/**
	 * Poses the next riddle.
	 * @param actor		Actor
	 * @param riddle	Riddle
	 * @throws ActionException if not in a riddle contest, it is not the actors turn, or the riddle has already been posed
	 */
	public Response riddle(Entity actor, Riddle riddle) throws ActionException {
		// Pose riddle
		final var model = find(actor);
		model.riddle(riddle);
		register(model);

		// Notify other player
		model.who().alert(new Description("riddle.contest.riddle", riddle.name()));
		return Response.EMPTY;
	}

	/**
	 * Answers the currently posed riddle.
	 * @param actor		Actor
	 * @param riddle	Answer
	 * @throws ActionException if not in a riddle contest, it is not the actors turn, or the answer is incorrect
	 */
	public Response answer(Entity actor, Riddle.Answer answer) throws ActionException {
		// Answer riddle
		final var model = find(actor);
		final var other = model.who();
		final var result = model.answer(answer);

		// Handle result
		switch(result) {
		case ACTIVE:
			// Wait for other player
			register(model);
			alert(model, Description.of("riddle.contest.pending"));
			return Response.of("riddle.contest.correct");

		case DRAWN:
			// Drawn contest
			cleanup(model);
			alert(model, DRAWN);
			return Response.of(DRAWN);

		default:
			// Contest finished
			cleanup(model);

			// Notify result
			if(model.who() == actor) {
				other.alert(LOST);
				return Response.of(WON);
			}
			else {
				other.alert(WON);
				return Response.of(LOST);
			}
		}
	}

	/**
	 * Resigns from the contest.
	 * @param actor Actor
	 * @throws ActionException if not in a riddle contest
	 */
	public Response resign(Entity actor) throws ActionException {
		// Resign contest
		final var model = find(actor);
		model.interrupt();
		model.who().alert(RESIGNED);
		cleanup(model);

		// Cleanup
		active.remove(actor);
		active.remove(model.who());

		// Build response
		return Response.of(RESIGNED);
	}

	/**
	 * Notifies the other player of a contest event.
	 * @param model			Riddle model
	 * @param message		Message
	 */
	private static void alert(Riddle.Model model, Description message) {
		model.who().alert(message);
	}

	/**
	 * Cleans up a completed contest.
	 * @param model Completed contest
	 */
	private void cleanup(Riddle.Model model) {
		active.removeValue(model);
		model.holder().cancel();
	}
}
