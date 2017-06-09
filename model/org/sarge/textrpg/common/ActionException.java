package org.sarge.textrpg.common;

/**
 * Exception indicating that a player action cannot logically be performed.
 * @author Sarge
 */
public class ActionException extends Exception {
	private final Object arg;
	
	/**
	 * Constructor.
	 * @param reason Reason identifier
	 */
	public ActionException(String reason) {
		this(reason, null);
	}
	
	/**
	 * Constructor with an additional argument.
	 * @param reason	Reason identifier
	 * @param arg		Argument
	 */
	public ActionException(String reason, Object arg) {
		super(reason.toLowerCase());
		this.arg = arg;
	}

	/**
	 * @return Argument
	 */
	public Object getArgument() {
		return arg;
	}
}
