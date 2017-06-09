package org.sarge.textrpg.object;

import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;

/**
 * Post-box model.
 * @author Sarge
 */
public class PostManager {
	/**
	 * Letter.
	 */
	public static class Letter {
		private final String sender;
		private final String text;
		private final Optional<ObjectDescriptor> attachment;
		private final long time;
		
		private boolean read;

		/**
		 * Constructor.
		 * @param sender			Name of sender
		 * @param text				Letter text
		 * @param attachment		Optional attachment
		 * @param time				Creation time
		 */
		public Letter(String sender, String text, ObjectDescriptor attachment, long time) {
			Check.notEmpty(sender);
			Check.notEmpty(text);
			Check.zeroOrMore(time);
			this.sender = sender;
			this.text = text;
			this.attachment = Optional.ofNullable(attachment);
			this.time = time;
		}

		/**
		 * Describes this letter.
		 * @return Description
		 */
		public Description describe() {
			return new Description.Builder("letter")
				.add("sender", sender)
				.add("text", text)
				.add("time", time)
				.add("read", read)
				.build();
		}
		
		/**
		 * Sets whether this letter has been read.
		 * @param read Read mark
		 */
		protected void setRead(boolean read) {
			this.read = read;
		}
		
		@Override
		public String toString() {
			return ToString.toString(this);
		}
	}
	
	/**
	 * Retrieves letters for the given actor.
	 * @param actor Actor
	 * @return Letters
	 */
	public Stream<Letter> getLetters(Actor actor) {
		// TODO
		return null;
	}
	
	/**
	 * Retrieves the specified letter.
	 * @param index Index
	 */
	public Optional<Letter> getLetter(int index) {
		// TODO
		return null;
	}

	/**
	 * Sends a letter to the given recipient.
	 * @param recipient		Name of recipient
	 * @param letter		Letter to send
	 */
	public void send(Actor recipient, Letter letter) {
		// TODO
	}
}
