package org.sarge.textrpg.util;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.AbstractObject;
import org.sarge.lib.util.Check;

/**
 * A <i>word cursor</i> is used to traverse a command line.
 * <p>
 * Example usage:
 * <pre>
 *  // Create cursor
 *  final WordCursor cursor = new WordCursor(line, stop);
 *
 *  // Extract next word
 *  final String next = cursor.next();
 *
 *  // Match next word
 *  final boolean matches = cursor.matches(...);
 *
 *  // Reset cursor to start of line
 *  cursor.reset();
 *
 *  // Check all words used
 *  cursor.isExhausted();
 * </pre>
 * <p>
 * @author Sarge
 */
public class WordCursor extends AbstractObject {
	// Configuration
	private final String line;
	private final NameStore store;
	private final Set<String> stop;

	// Word iterator
	private final BreakIterator itr = BreakIterator.getWordInstance();
	private int start;

	// Words
	private final List<String> words = new ArrayList<>();
	private int index;
	private Integer mark;

	/**
	 * Constructor.
	 * @param line			Command line
	 * @param store			Name-store
	 * @param stop			Tests whether a word is a stop-word
	 */
	public WordCursor(String line, NameStore store, Set<String> stop) {
		this.stop = notNull(stop);
		this.store = notNull(store);
		this.line = notEmpty(line);
		itr.setText(line);
		start = itr.first();
	}

	/**
	 * @return Name-store
	 */
	public NameStore store() {
		return store;
	}

	/**
	 * @return Whether all words have been consumed
	 */
	public boolean isExhausted() {
		if(index < words.size()) {
			return false;
		}
		else {
			return start == BreakIterator.DONE;
		}
	}

	/**
	 * Tests whether this cursor has at least the required number of words.
	 * @param num Required number of words
	 * @return Whether sufficient capacity
	 */
	public boolean capacity(int num) {
		Check.zeroOrMore(num);
		if(num == 0) return true;
		return ensure(num - words.size());
	}

	/**
	 * Tests whether this cursor has at least the required number of <b>remaining</b> words.
	 * @param num Required number of words
	 * @return Whether sufficient capacity
	 */
	public boolean remaining(int num) {
		Check.zeroOrMore(num);
		if(num == 0) return true;
		final int required = num - (words.size() - index);
		return ensure(required);
	}

	/**
	 * Ensures this cursor has at least the given number of words in addition to those already extracted from the command line.
	 * @param num Number of additional words
	 * @return Whether has sufficient number of words
	 */
	private boolean ensure(int num) {
		// Short-cut test
		if(num <= 0) return true;

		// Extract additional words
		for(int n = 0; n < num; ++n) {
			while(true) {
				// Stop if insufficient words
				final int end = itr.next();
				if(end == BreakIterator.DONE) {
					return false;
				}

				// Extract next word
				final String word = line.substring(start, end);
				start = itr.next();

				// Ignore stop words
				if(stop.contains(word)) {
					continue;
				}

				// Add extracted word
				words.add(word);
				break;
			}
		}

		// Sufficient capacity
		return true;
	}

	/**
	 * Gets the next word.
	 * @return Next word
	 * @throws NoSuchElementException if this cursor does not have sufficient capacity
	 * @see #remaining(int)
	 */
	public String next() {
		if(index >= words.size()) {
			if(!ensure(1)) throw new NoSuchElementException();
		}

		return words.get(index++);
	}

	/**
	 * Helper - Matches the given string argument against the next word in this cursor.
	 * @return Whether matched
	 * @throws NoSuchElementException if this cursor does not have sufficient capacity
	 * @see #next()
	 * @see NameStore#matches(String, String)
	 */
	public boolean matches(String name) {
		return store.matches(name, next());
	}

	/**
	 * Marks the current cursor position.
	 * @see #back()
	 */
	public void mark() {
		mark = index;
	}

	/**
	 * Restores this cursor to the previous mark.
	 * @throws IllegalStateException if there is no previous mark to restore
	 * @see #mark()
	 */
	public void back() {
		if(mark == null) throw new IllegalStateException("No mark to restore: " + this);
		index = mark;
		mark = null;
	}

	/**
	 * Resets this cursor to the first word.
	 */
	public void reset() {
		index = 0;
		mark = null;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("words", words)
			.append("index", index)
			.append("start", start)
			.append("mark", mark)
			.append("line", TextHelper.wrap(line, '[', ']'))
			.toString();
	}
}
