package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Set;

import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Event;

/**
 * Riddle.
 * @author Sarge
 */
public final class Riddle extends AbstractEqualsObject implements CommandArgument {
	/**
	 * Riddle answer.
	 */
	public final class Answer extends AbstractEqualsObject implements CommandArgument {
		private final String ans;

		/**
		 * Constructor.
		 * @param answer Answer
		 */
		private Answer(String answer) {
			this.ans = notEmpty(answer);
		}

		/**
		 * @return Answer to this riddle
		 */
		@Override
		public String name() {
			return ans;
		}

		@Override
		public String toString() {
			return ans;
		}
	}

	private final String name;
	private final Answer answer;

	/**
	 * Constructor.
	 * @param name			Riddle name
	 * @param answer		Answer
	 */
	public Riddle(String name, String answer) {
		this.name = notEmpty(name);
		this.answer = new Answer(answer);
	}

	/**
	 * @return Riddle name
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @return Answer to this riddle
	 */
	public Answer answer() {
		return answer;
	}

	/**
	 * Riddle contest model.
	 */
	public static class Model extends AbstractEqualsObject {
		/**
		 * Contest state.
		 */
		public enum State {
			/**
			 * Waiting for the challenged entity to accept.
			 */
			OPEN,

			/**
			 * Waiting for a riddle to be posed.
			 */
			PENDING,

			/**
			 * Waiting for the answer.
			 */
			WAITING,

			/**
			 * Contest has finished.
			 */
			FINISHED
		}

		/**
		 * Winner of this contest.
		 */
		public enum Result {
			ACTIVE,
			FIRST,
			SECOND,
			DRAWN
		}

		private final Entity one, two;
		private final int limit;
		private final Set<Riddle> used = new StrictSet<>();
		private final Event.Holder holder = new Event.Holder();

		private State state = State.OPEN;
		private boolean first = true;
		private int round;
		private Riddle riddle;

		/**
		 * Constructor.
		 * @param one		Challenger
		 * @param two		Challenged entity
		 * @param limit		Number of rounds
		 */
		public Model(Entity one, Entity two, int limit) {
			this.one = notNull(one);
			this.two = notNull(two);
			this.limit = oneOrMore(limit);
		}

		/**
		 * @return Current round
		 */
		public int round() {
			return round;
		}

		/**
		 * @return Number of rounds in this contest
		 */
		public int limit() {
			return limit;
		}

		/**
		 * @return State of this contest
		 */
		public State state() {
			return state;
		}

		/**
		 * @return Entity whose turn it is
		 */
		public Entity who() {
			switch(state) {
			case OPEN:			return two;
			case PENDING:		return who(first);
			case WAITING:		return who(!first);
			default:			throw new RuntimeException();
			}
		}

		/**
		 * Helper.
		 */
		private Entity who(boolean first) {
			return first ? one : two;
		}

		/**
		 * @return Timeout event holder
		 */
		public Event.Holder holder() {
			return holder;
		}

		/**
		 * Accepts the contest.
		 * @throws ActionException if already accepted
		 */
		public void accept() throws ActionException {
			if(state != State.OPEN) throw ActionException.of("accept.already.accepted");
			state = State.PENDING;
		}

		/**
		 * Poses the next riddle.
		 * @param riddle Riddle
		 * @throws ActionException if not waiting for a riddle or it has already been used in this contest
		 */
		public void riddle(Riddle riddle) throws ActionException {
			Check.notNull(riddle);
			if(state != State.PENDING) throw ActionException.of("riddle.not.pending");
			if(used.contains(riddle)) throw ActionException.of("riddle.already.used");
			this.riddle = riddle;
			state = State.WAITING;
			used.add(riddle);
		}

		/**
		 * Answers the current riddle.
		 * @param answer Offered answer
		 * @return Result
		 * @throws ActionException if not waiting for an answer
		 */
		public Result answer(Answer answer) throws ActionException {
			Check.notNull(answer);
			if(state != State.WAITING) throw ActionException.of("riddle.not.waiting");

			if(riddle.answer.equals(answer)) {
				// Correct answer
				if(first) {
					// Other players turn
					return next();
				}
				else {
					// First players turn
					if(++round >= limit) {
						// Contest drawn
						state = State.FINISHED;
						return Result.DRAWN;
					}
					else {
						// Wait for next riddle
						return next();
					}
				}
			}
			else {
				// Incorrect answer
				return winner();
			}
		}

		/**
		 * Interrupts an active contest.
		 * @return Winner
		 */
		public Result interrupt() {
			switch(state) {
			case PENDING:
			case WAITING:
				first = !first;
				return winner();

			case FINISHED:
				throw new IllegalStateException("Contest already finished");

			default:
				state = State.FINISHED;
				return Result.DRAWN;
			}
		}

		/**
		 * Waits for next riddle to be posed.
		 */
		private Result next() {
			first = !first;
			state = State.PENDING;
			return Result.ACTIVE;
		}

		/**
		 * Finishes this contest.
		 * @return Winner
		 */
		private Result winner() {
			assert (state == State.PENDING) || (state == State.WAITING);
			state = State.FINISHED;
			return first ? Result.FIRST : Result.SECOND;
		}
	}
}
