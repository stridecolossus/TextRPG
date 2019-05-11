package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Parser result.
 * @author Sarge
 */
public class ParserResult extends AbstractEqualsObject {
	/**
	 * Failed parser result.
	 */
	public static final ParserResult FAILED = new ParserResult(ParserResult.Reason.SYNTAX);

	/**
	 * Merges a set of failure reasons.
	 * @param reasons Reason(s)
	 * @return Merged result
	 */
	public static ParserResult merge(List<Reason> reasons) {
		for(Reason reason : reasons) {
			switch(reason) {
			case SUCCESS:
				throw new IllegalStateException("Cannot merge a successful result");

			case MISMATCH:
				return new ParserResult(Reason.MISMATCH);
			}
		}

		return ParserResult.FAILED;
	}

	/**
	 * Parse result reason.
	 */
	public enum Reason {
		/**
		 * Successfully parsed.
		 */
		SUCCESS,

		/**
		 * Syntactically incorrect.
		 */
		SYNTAX,

		/**
		 * Mismatched argument type, i.e. syntactically correct but illogical.
		 */
		MISMATCH
	}

	private final Reason reason;
	private final Command command;

	/**
	 * Constructor for a successful result.
	 * @param command Command
	 */
	public ParserResult(Command command) {
		this(Reason.SUCCESS, notNull(command));
	}

	/**
	 * Constructor for a failed result.
	 * @param reason Failure reason
	 */
	public ParserResult(Reason reason) {
		this(reason, null);
	}

	/**
	 * Constructor.
	 * @param reason		Reason
	 * @param command		Command
	 */
	private ParserResult(Reason reason, Command command) {
		this.reason = notNull(reason);
		this.command = command;
	}

	/**
	 * @return Whether this is a successfully parsed result
	 */
	public boolean isParsed() {
		return reason == Reason.SUCCESS;
	}

	/**
	 * @return Reason for a failed result
	 */
	public Reason reason() {
		return reason;
	}

	/**
	 * @return Command
	 */
	public Command command() {
		return command;
	}
}
