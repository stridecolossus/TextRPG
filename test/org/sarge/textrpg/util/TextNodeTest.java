package org.sarge.textrpg.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.util.TextNode.Builder;

public class TextNodeTest {
	private TextNode node;
	
	@Before
	public void before() {
		node = new TextNode("node", "value", null);
	}
	
	@Test
	public void constructor() {
		assertEquals("node", node.name());
		assertEquals("value", node.value());
		assertEquals(null, node.parent());
		assertNotNull(node.children());
		assertEquals(0, node.children().count());
	}
	
	@Test
	public void child() {
		final TextNode child = new TextNode("child", null, node);
		assertEquals(node, child.parent());
		assertEquals(1, node.children().count());
		assertEquals(child, node.children().iterator().next());
		assertEquals(child, node.child());
		assertEquals(child, node.child("child"));
		assertArrayEquals(new TextNode[]{child}, node.children("child").toArray());
		assertEquals(Optional.of(child), node.optionalChild());
		assertEquals(Optional.empty(), child.optionalChild());
	}
	
	@SuppressWarnings("unused")
	@Test
	public void attributes() {
		new TextNode("integer", "42", node);
		new TextNode("boolean", "true", node);
		assertEquals("42", node.getValue("integer"));
		assertEquals("42", node.getString("integer", null));
		assertEquals(42, node.getInteger("integer", null));
		assertEquals(42L, node.getLong("integer", null));
		assertEquals(42f, node.getFloat("integer", null), 0.001f);
		assertEquals(true, node.getBoolean("boolean", null));
	}
	
	@Test
	public void builder() {
		final TextNode child = new Builder("child").parent(node).value("value").build();
		assertNotNull(child);
		assertEquals("child", child.name());
		assertEquals("value", child.value());
		assertEquals(node, child.parent());
	}
}
