package org.sarge.textrpg.common;

import org.sarge.lib.util.Check;

/**
 * Message notification.
 * @author Sarge
 */
public final class Message extends AbstractNotification {
	private final Description description;
	
	/**
	 * Constructor.
	 * @param message Message
	 * @return Message
	 */
	public Message(String message) {
		this(new Description(message));
	}
	
	/**
	 * Constructor for a message with an additional argument.
	 * @param message Message
	 * @return Message
	 */
	public Message(String message, Object arg) {
		this(new Description(message, "arg", arg));
	}

	/**
	 * Constructor.
	 * @param description Message description
	 */
	public Message(Description description) {
		Check.notNull(description);
		this.description = description;
	}

	/**
	 * Creates an action error notification.
	 * @param e Action exception
	 * @return Error message
	 */
	public static Message of(ActionException e) {
		final Object arg = e.getArgument();
		if(arg == null) {
			return new Message(e.getMessage());
		}
		else {
			return new Message(e.getMessage(), arg);
		}
	}

	@Override
	public Description describe() {
		return description;
	}
	
	@Override
	public void accept(Handler handler) {
		handler.handle(this);
	}
}
