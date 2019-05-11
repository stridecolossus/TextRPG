package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.object.Readable.Section;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;

public class ReadableTest {
	@Nested
	class Simple {
		private Readable readable;

		@BeforeEach
		public void before() {
			readable = new Readable(new Readable.Descriptor(ObjectDescriptor.of("readable"), Skill.NONE));
		}

		@Test
		public void constructor() {
			assertEquals(Skill.NONE, readable.descriptor().language());
			assertEquals(false, readable.descriptor().isBook());
			assertEquals(1, readable.descriptor().sections().size());
		}

		@Test
		public void section() {
			final Section section = readable.descriptor().sections().get(0);
			assertEquals(new Section(null, "readable", false), section);
		}

		@Test
		public void describe() {
			final Description expected = new Description("readable.text");
			assertEquals(expected, readable.descriptor().sections().get(0).describe(ArgumentFormatter.PLAIN));
		}
	}

	@Nested
	class Book {
		private Readable book;

		@BeforeEach
		public void before() {
			final Section section = new Section("title", "text", false);
			book = new Readable(new Readable.Descriptor(ObjectDescriptor.of("readable"), true, Skill.NONE, List.of(section, section)));
		}

		@Test
		public void constructor() {
			assertEquals(Skill.NONE, book.descriptor().language());
			assertEquals(true, book.descriptor().isBook());
			assertEquals(2, book.descriptor().sections().size());
		}
	}
}
