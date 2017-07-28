package org.sarge.textrpg.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.util.TextParser.ParserException;

public class TextParserTest {
	private TextParser parser;
	
	@Before
	public void before() {
		parser = new TextParser();
	}
	
	@Test
	public void parseSimpleNode() throws IOException {
		final TextNode node = parser.parse(new StringReader("node"));
		assertNotNull(node);
		assertEquals("node", node.name());
		assertEquals(null, node.value());
		assertNotNull(node.children());
		assertEquals(0, node.children().count());
	}

	@Test
	public void parseNameValue() throws IOException {
		final TextNode node = parser.parse(new StringReader("node value"));
		assertNotNull(node);
		assertEquals("node", node.name());
		assertEquals("value", node.value());
		assertEquals(0, node.children().count());
	}

	@Test
	public void parseChild() throws IOException {
		final TextNode node = parser.parse(new StringReader("node { \n name value \n }"));
		assertNotNull(node);
		assertEquals("node", node.name());
		assertEquals(1, node.children().count());
		assertEquals("name", node.children().iterator().next().name());
		assertEquals("value", node.children().iterator().next().value());
		assertEquals("value", node.getValue("name"));
	}

	@Test
	public void parseSiblings() throws IOException {
		final TextNode node = parser.parse(new StringReader("parent { \n one \n two \n }"));
		assertNotNull(node);
		assertEquals("parent", node.name());
		assertEquals(2, node.children().count());
		assertEquals("name", node.children().iterator().next().name());
		assertEquals("value", node.children().iterator().next().value());
		assertEquals("value", node.getValue("name"));
	}

	@Test
	public void parseSkipCommentsLines() throws IOException {
		final TextNode node = parser.parse(new StringReader("# comment \n node"));
		assertNotNull(node);
		assertEquals("node", node.name());
	}

	@Test
	public void parseSkipCommentSuffix() throws IOException {
		final TextNode node = parser.parse(new StringReader("node # comment"));
		assertNotNull(node);
		assertEquals("node", node.name());
	}

	@Test(expected = ParserException.class)
	public void parseEmpty() throws IOException {
		parser.parse(new StringReader(""));
	}
	
	@Test(expected = ParserException.class)
	public void parseEmptyEndSection() throws IOException {
		parser.parse(new StringReader("}"));
	}
	
	@Test(expected = ParserException.class)
	public void parseInvalidHeaderToken() throws IOException {
		parser.parse(new StringReader("node { cobblers"));
	}
	
	@Test(expected = ParserException.class)
	public void parseInvalidToken() throws IOException {
		parser.parse(new StringReader("node value cobblers"));
	}
}
