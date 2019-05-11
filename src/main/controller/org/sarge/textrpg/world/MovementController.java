package org.sarge.textrpg.world;

import static java.util.stream.Collectors.toCollection;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.MovementMode;
import org.sarge.textrpg.entity.Transaction;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for a moving entity.
 * @author Sarge
 */
@Controller
public class MovementController {
	/**
	 * Result of a movement requirement.
	 */
	public static final class Result extends AbstractEqualsObject {
		/**
		 * Default successful movement result.
		 */
		public static final Result DEFAULT = new Result(null, 0);

		private final String reason;
		private final Optional<Description> description;
		private final int cost;

		/**
		 * Constructor for a successful move.
		 * @param description		Optional description
		 * @param cost				Movement cost
		 */
		public Result(Description description, int cost) {
			this(null, description, cost);
		}

		/**
		 * Constructor for a failed move.
		 * @param reason Reason code
		 */
		public Result(String reason) {
			this(reason, null, 0);
		}

		/**
		 * Constructor.
		 * @param reason			Reason if this requirement is not met
		 * @param description		Optional description
		 * @param cost				Additional movement cost
		 */
		private Result(String reason, Description description, int cost) {
			this.reason = reason;
			this.description = Optional.ofNullable(description);
			this.cost = zeroOrMore(cost);
		}

		/**
		 * @return Whether this is a successful result
		 */
		public boolean isSuccess() {
			return reason == null;
		}

		/**
		 * @return Reason code if this requirement is not met
		 */
		protected String reason() {
			return reason;
		}

		/**
		 * @return Description
		 */
		public Optional<Description> description() {
			return description;
		}
	}

	/**
	 * Movement requirement.
	 */
	@FunctionalInterface
	public interface Requirement {
		/**
		 * Applies this requirement to a candidate move and generates a result.
		 * @param actor		Actor
		 * @param exit		Exit being traversed
		 * @return Result
		 */
		Result result(Entity actor, Exit exit);
	}

	/**
	 * Listener for successfully moved entities.
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Notifies an entity that has moved.
		 * @param actor		Actor
		 * @param exit		Traversed exit
		 * @param prev		Previous location
		 */
		void update(Entity actor, Exit exit, Location prev);

		/**
		 * @return Whether this listener only applies to players
		 */
		default boolean isPlayerOnly() {
			return false;
		}
	}

	@Autowired private final List<Requirement> requirements = new StrictList<>();
	@Autowired private final Set<Listener> listeners = new StrictSet<>();

	/**
	 * Adds a movement requirement.
	 * @param req Requirement
	 */
	public void add(Requirement req) {
		requirements.add(req);
	}

	/**
	 * Adds a movement listener.
	 * @param listeners Listener to add
	 */
	public void add(Listener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Attempts to move an entity through the given exit.
	 * @param actor			Actor
	 * @param exit			Exit to traverse
	 * @param mod			Movement cost modifier
	 * @return Results
	 * @throws ActionException if any requirement fails or the exit cannot be traversed by the actors movement mode
	 * @throws IllegalArgumentException if no requirements are configured
	 * @throws IllegalArgumentException if the exit cannot be traversed
	 * @see MovementMode#move(Exit)
	 * @see Link#isTraversable()
	 */
	public List<Result> move(Entity actor, Exit exit, int mod) throws ActionException {
		if(requirements.isEmpty()) throw new IllegalStateException("No movement requirements");
		if(!exit.link().isTraversable()) throw new IllegalArgumentException("Exit cannot be traversed: " + exit);

		// Test requirements
		final var results = requirements.stream().map(req -> req.result(actor, exit)).takeWhile(Result::isSuccess).collect(toCollection(LinkedList::new));

		// Stop if requirements not met
		final Result last = results.getLast();
		if(!last.isSuccess()) {
			throw ActionException.of(last.reason());
		}

		// Calculate overall stamina cost
		final int cost = mod * results.stream().mapToInt(e -> e.cost).sum();

		// Init stamina transactions
		final MovementMode mode = actor.movement();
		final var transactions = mode.transactions(cost);
		for(Transaction tx : transactions) {
			tx.check();
		}

		// Traverse exit
		final Location prev = actor.location();
		actor.movement().move(exit);

		// Consume stamina
		transactions.forEach(Transaction::complete);
		// TODO - tired warning(s)

		// Notify listeners
		notify(actor, exit, prev);

		return results;
	}

	/**
	 * Notifies all movement listeners of a successfully moved entity.
	 * @param actor		Actor
	 * @param exit		Traversed exit
	 * @param prev		Previous location
	 */
	public void notify(Entity actor, Exit exit, Location prev) {
		if(actor.isPlayer()) {
			listeners.forEach(e -> e.update(actor, exit, prev));
		}
		else {
			listeners.stream().filter(StreamUtil.not(Listener::isPlayerOnly)).forEach(e -> e.update(actor, exit, prev));
		}
	}
}
