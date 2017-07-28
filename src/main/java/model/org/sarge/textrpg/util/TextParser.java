package org.sarge.textrpg.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.util.TextNode.Builder;

/**
 * Parser for a {@link TextNode} document.
 * <p>
 * A text-node has the following format:
 * <pre>
 * 		name value {
 * 			children
 * 		}
 * </pre>
 * Notes:
 * <ul>
 * <li>All tokens are space-delimited</li>
 * <li>The brace delimiters are optional if a text-node does not have any children</li>
 * <li>Tokens are alphanumeric including the full-stop character</li>
 * <li>The hash character indicates comments and can be used at the start or end of a line</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 *		{@literal #} A comment
 * 		bag.end {
 * 			terrain indoors            {@literal #} Attribute
 * 			east the.garden            {@literal #} Node with multiple type-qualifiers
 * 			bag.end.key                {@literal #} Leaf node
 * 			low.place fixture {        {@literal #} Child node
 * 				size small
 * 			}
 * 		}
 *
 * 		party.tree fixture             {@literal #} Another leaf node
 * </pre>
 * @author Sarge
 */
public class TextParser {
	private static final String COMMENT = "#";

	/**
	 * Parser exception.
	 */
	public class ParserException extends RuntimeException {
		private int lineno;

		/**
		 * Constructor.
		 * @param message
		 */
		private ParserException(String message) {
			super(message);
		}

		/**
		 * Error constructor.
		 * @param e				Exception
		 * @param lineno		Line-number
		 */
		private ParserException(Exception e, int lineno) {
			super(e);
			this.lineno = lineno;
		}

		@Override
		public String getMessage() {
			if(lineno == 0) {
				return super.getMessage();
			}
			else {
				return super.getMessage() + " at line " + lineno;
			}
		}
	}

	/**
	 * Stack entry.
	 */
	private class Entry {
		private final Builder builder;
		private final List<Entry> children = new ArrayList<>();

		public Entry(Builder builder) {
			this.builder = builder;
		}
	}

	private final LinkedList<Entry> stack = new LinkedList<>();
	private Entry current;

	/**
	 * Parses the given text node(s).
	 * @param in Input
	 * @return Root node
	 * @throws IOException
	 * @throws ParserException if the input is not valid
	 */
	public TextNode parse(Reader in) throws IOException {
		// Parse text
		try(final LineNumberReader r = new LineNumberReader(in)) {
			try {
				r.lines()
					.map(String::trim)
					.filter(StreamUtil.not(String::isEmpty))
					.filter(line -> !line.startsWith(COMMENT))
					.forEach(this::parse);
			}
			catch(final ParserException e) {
				e.lineno = r.getLineNumber();
				throw e;
			}
			catch(final Exception e) {
				final ParserException ex = new ParserException(e, r.getLineNumber());
				throw ex;
			}
		}

		// Construct tree
		if(current == null) throw new ParserException("Empty document");
		current = stack.pop();
		if(!stack.isEmpty()) throw new ParserException("Incomplete node tree");
		return build(current, null);
	}

	/**
	 * Parses the given line.
	 * @param line Line to parse
	 */
	private void parse(String line) {
		try(final Scanner sc = new Scanner(line)) {
			// Init
			sc.useDelimiter("\\s+");

			// Check for end-of-section
			if(sc.hasNext("\\}")) {
				sc.next();
				check(sc);
				System.out.println("pop current="+current.builder+" stack="+stack.peek().builder);
				current = stack.pop();
				return;
			}

			// Start new node
			final Builder builder = new Builder(sc.next());
			final Entry entry = new Entry(builder);

			// Push parent
			//if(current != null) {
				if(current != null) {
					System.out.println("push current="+(current==null?"null":current.builder)+" entry="+entry.builder);
					current.children.add(entry);
				}
			//}
			current = entry;
			stack.push(current);

			// Parse remainder of node header
			boolean hasValue = false;
			if(sc.hasNext()) {
				while(true) {
					final String token = sc.next();
					switch(token) {
					case "{":
						// Finished node header
						System.out.println("start-section");
						check(sc);
						return;

					case COMMENT:
						// Skip end-of-line comment
						sc.nextLine();
						return;

					default:
						// Load optional value
						System.out.println("value="+token);
						if(hasValue) throw new ParserException("Unexpected token: " + token);
						builder.value(token);
						hasValue = true;
						break;
					}

					if(!sc.hasNext()) {
						System.out.println("pop-sibling current="+current.builder+" stack="+stack.peek().builder);
						current = stack.pop();
						break;
					}
				}
			}
		}
	}

	/**
	 * Verifies that the remainder of the line is empty or a comment.
	 */
	private void check(Scanner sc) {
		if(sc.hasNext()) {
			final String token = sc.next();
			if(!token.equals(COMMENT)) throw new ParserException("Expected end-of-line comment");
		}
	}

	/**
	 * Recursively builds a node-tree.
	 * @param entry Stack entry
	 * @return Root node
	 */
	private static TextNode build(Entry entry, TextNode parent) {
		final TextNode node = entry.builder.parent(parent).build();
		for(final Entry child : entry.children) {
			build(child, node);
		}
		return node;
	}

	/*
	public static void main(String[] args) throws IOException {
		final TextParser parser = new TextParser();
		final TextNode root = parser.parse(new FileReader("./resources/working/structured.txt"));
		dump(root, 0);
	}

	private static void dump(TextNode node, int indent) {
		for(int n = 0; n < indent; ++n) {
			System.out.print("  ");
		}
		System.out.println(node.name()+" type="+node.types().collect(toList())+" attrs="+node.attributes());
		node.children().forEach(child -> dump(child, indent + 1));
	}
	*/
}
