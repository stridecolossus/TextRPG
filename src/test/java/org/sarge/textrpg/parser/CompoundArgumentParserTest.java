package org.sarge.textrpg.parser;

import org.junit.jupiter.api.Disabled;

@Disabled
public class CompoundArgumentParserTest {
//	/**
//	 * Mock implementation.
//	 */
//	private class MockParser extends CompoundArgumentParser<Integer> {
//		private final Supplier<Integer> num;
//
//		public MockParser() {
//			add(new StringArgumentParser("literal"));
//			num = add(new IntegerArgumentParser());
//		}
//
//		@Override
//		protected Integer build() {
//			return num.get();
//		}
//	}
//
//	private CompoundArgumentParser<Integer> parser;
//
//	@BeforeEach
//	public void before() {
//		parser = new MockParser();
//	}
//
//	private static ArgumentCursor cursor(String command) {
//		final var words = Arrays.asList(command.split(" "));
//		return new ArgumentCursor(words, Translator.IDENTITY);
//	}
//
//	@Test
//	public void parse() {
//		assertEquals(Integer.valueOf(42), parser.parse(cursor("literal 42")));
//	}
//
//	@Test
//	public void parseInsufficientWords() {
//		assertThrows(IllegalStateException.class, () -> parser.parse(cursor("literal")));
//	}
//
//	@Test
//	public void parseFailed() {
//		assertEquals(null, parser.parse(cursor("literal cobblers")));
//	}
//
//	@Test
//	public void parseEmpty() {
//		final CompoundArgumentParser<Integer> empty = new CompoundArgumentParser<>() {
//			@Override
//			protected Integer build() {
//				return null;
//			}
//		};
//		assertThrows(IllegalArgumentException.class, () -> empty.parse(null));
//	}
}
