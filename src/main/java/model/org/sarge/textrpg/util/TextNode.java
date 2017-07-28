package org.sarge.textrpg.util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ConverterAdapter;

/**
 * A text node is a tree structure of name-value pairs.
 */
public final class TextNode extends ConverterAdapter {
	/**
	 * Text-node exception.
	 */
	public class TextException extends RuntimeException {
		private TextException(String reason) {
			super(reason + " at " + path(TextNode.this));
		}
	}

	/**
	 * Builds path to the given node.
	 */
	private static String path(TextNode node) {
		final List<TextNode> path = new ArrayList<>();
		TextNode n = node;
		while(n != null) {
			path.add(n);
			n = n.parent;
		}
		Collections.reverse(path);
		return path.stream().map(TextNode::name).collect(joining("/"));
	}

	private final String name;
	private final String value;
	private final TextNode parent;
	private final List<TextNode> children = new ArrayList<>();

	/**
	 * Constructor.
	 * @param name			Node name
	 * @param value			Optional value
	 * @param parent		Parent node or <tt>null</tt> for a root node
	 */
	public TextNode(String name, String value, TextNode parent) {
		Check.notEmpty(name);
		this.name = name;
		this.value = value;
		this.parent = parent;
		if(parent != null) {
			parent.children.add(this);
		}
	}

	/**
	 * Convenience constructor for a simple root node.
	 * @param name Root node name
	 */
	public TextNode(String name) {
		this(name, null, null);
	}
	
	/**
	 * @return Node name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Node value
	 */
	public String value() {
		return value;
	}

	/**
	 * @return Parent node or <tt>null</tt> if root node
	 */
	public TextNode parent() {
		return parent;
	}

	/**
	 * Retrieves a child attribute value.
	 */
	@Override
	public String getValue(String name) {
		return getChild(name).map(TextNode::value).orElse(null);
	}
	
	/**
	 * Retrieves a child by name.
	 */
	private Optional<TextNode> getChild(String name) {
		return children.stream().filter(node -> node.name.equals(name)).findFirst();
	}

	/**
	 * @return Child nodes
	 */
	public Stream<TextNode> children() {
		return children.stream();
	}
	
	/**
	 * @param name Node name
	 * @return Child nodes with the given name
	 */
	public Stream<TextNode> children(String name) {
		return children.stream().filter(node -> node.name.equals(name));
	}
	
	/**
	 * @return Mandatory single child node
	 * @throws IllegalArgumentException if this node does not have a <b>single</b> node
	 */
	public TextNode child() {
		if(children.size() != 1) throw exception("Expected single child element");
		return children.get(0);
	}

	/**
	 * @param name Node name
	 * @return Child node with the given name
	 */
	public TextNode child(String name) {
		return getChild(name).orElseThrow(() -> exception("Expected child: " + name));
	}

	/**
	 * @return Single optional child node
	 * @throws IllegalArgumentException if this node does not have a <b>single</b> node
	 */
	public Optional<TextNode> optionalChild() {
		return optionalChild(children);
	}
	
	/**
	 * @return Single optional child node
	 * @throws IllegalArgumentException if this node does not have a <b>single</b> node
	 */
	public Optional<TextNode> optionalChild(String name) {
		return optionalChild(children(name).collect(toList()));
	}

	private Optional<TextNode> optionalChild(List<TextNode> list) {
		switch(list.size()) {
		case 0: return Optional.empty();
		case 1: return Optional.of(list.get(0));
		default: throw exception("Unexpected multiple children");
		}
	}

	/**
	 * Raises an exception at this node.
	 * @param reason Reason
	 * @return Exception
	 */
	public TextException exception(String reason) {
		return new TextException(reason);
	}

	/**
	 * Raises an exception at this node.
	 * @param cause		Cause
	 * @param reason	Reason
	 * @return Exception
	 */
	public TextException exception(Throwable cause) {
		return new TextException(cause.getMessage());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Builder for a text node.
	 */
	public static class Builder {
		private final String name;
		private String value;
		private TextNode parent;

		/**
		 * Constructor.
		 * @param name Node name
		 */
		public Builder(String name) {
			Check.notEmpty(name);
			this.name = name;
		}
		
		/**
		 * Sets the value of this node.
		 * @param value Value
		 */
		public Builder value(String value) {
			this.value = value;
			return this;
		}

		/**
		 * Sets the parent of this node.
		 * @param parent Parent node
		 */
		public Builder parent(TextNode parent) {
			this.parent = parent;
			return this;
		}

		/**
		 * Completes this builder.
		 * @return New text node
		 */
		public TextNode build() {
			return new TextNode(name, value, parent);
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
