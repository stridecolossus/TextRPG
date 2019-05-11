package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;

/**
 * Bulletin board posting.
 * @author Sarge
 */
public final class Bulletin extends AbstractEqualsObject {
	/**
	 * Bulletin group.
	 */
	public enum Group {
		NEWS,
		FAQ,
		GENERAL,
	}

	/**
	 * Bulletin repository.
	 */
	public interface Repository {
		/**
		 * Looks up unread bulletins for the given player.
		 * @param name 		Player name
		 * @param group		Bulletin group
		 * @return Bulletin posts
		 */
		Stream<Bulletin> posts(String name, Group group);

		/**
		 * Marks the given bulletin as read.
		 * @param name			Player name
		 * @param bulletin		Bulletin
		 */
		void mark(String name, Bulletin bulletin);

		/**
		 * Marks <b>all</b> bulletins in the given group as read.
		 * @param name			Player name
		 * @param group			Bulletin group
		 */
		void mark(String name, Group group);

		/**
		 * Marks <b>all</b> bulletins in the given group as not read.
		 * @param name			Player name
		 * @param group			Bulletin group
		 */
		void clear(String name, Group group);
	}

	private final Group group;
	private final int index;
	private final String poster;
	private final String text;

	/**
	 * Constructor.
	 * @param poster		Poster
	 * @param group			Bulletin group
	 * @param index			Post index
	 * @param text			Text
	 */
	public Bulletin(Group group, int index, String poster, String text) {
		this.group = notNull(group);
		this.index = oneOrMore(index);
		this.poster = notEmpty(poster);
		this.text = notEmpty(text);
	}

	/**
	 * @return Bulletin index
	 */
	public int iIndex() {
		return index;
	}

	/**
	 * @return Bulletin group
	 */
	public Group group() {
		return group;
	}

	/**
	 * @return Poster
	 */
	public String poster() {
		return poster;
	}

	/**
	 * @return Bulletin text
	 */
	public String text() {
		return text;
	}

	/**
	 * @return Describes this bulletin posting.
	 */
	public Description describe() {
		return new Description.Builder("bulletin.pos")
			.add("index", index)
			.add("poster", poster, ArgumentFormatter.PLAIN)
			.add("text", text, ArgumentFormatter.PLAIN)
			.build();
	}
}
