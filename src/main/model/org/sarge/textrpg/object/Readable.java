package org.sarge.textrpg.object;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * A <i>readable</i> is an object that can be read such as a letter, book or plaque.
 * @author Sarge
 */
public class Readable extends WorldObject {
	/**
	 * Section or chapter of this readable.
	 */
	public static final class Section extends AbstractEqualsObject {
		private final String title;
		private final String text;
		private final boolean hidden;

		/**
		 * Constructor.
		 * @param title		Optional title
		 * @param text		Text
		 * @param hidden	Whether this section is hidden
		 */
		public Section(String title, String text, boolean hidden) {
			this.title = title;
			this.text = notEmpty(text);
			this.hidden = hidden;
		}

		/**
		 * @return Chapter title
		 */
		public String title() {
			return title;
		}

		/**
		 * @return Chapter text
		 */
		public String text() {
			return text;
		}

		/**
		 * @return Whether this section is hidden
		 */
		public boolean isHidden() {
			return hidden;
		}

		/**
		 * Describes this section.
		 * @return Description
		 */
		public Description describe(ArgumentFormatter formatter) {
			final String name = TextHelper.join(text, "text");
			if(title == null) {
				return new Description(name);
			}
			else {
				return new Description.Builder("readable.section")
					.add("title", title, formatter)
					.add("text", name, formatter)
					.build();
			}
		}
	}

	/**
	 * Readable descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final boolean book;
		private final Skill lang;
		private final List<Section> sections;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param book				Whether this readable is a book
		 * @param lang				Language skill
		 * @param sections			Text section(s)
		 */
		public Descriptor(ObjectDescriptor descriptor, boolean book, Skill lang, List<Section> sections) {
			super(descriptor);
			this.book = book;
			this.lang = notNull(lang);
			this.sections = List.copyOf(sections);
		}

		/**
		 * Convenience constructor for a simple readable.
		 * @param descriptor		Object descriptor
		 * @param lang				Language skill
		 */
		public Descriptor(ObjectDescriptor descriptor, Skill lang) {
			this(descriptor, false, lang, List.of(new Section(null, descriptor.name(), false)));
		}

		/**
		 * @return Whether this readable is a book
		 */
		public boolean isBook() {
			return book;
		}

		/**
		 * @return Language that this readable is written in
		 */
		public Skill language() {
			return lang;
		}

		/**
		 * @return Sections of this readable
		 */
		public List<Section> sections() {
			return sections;
		}

		@Override
		public Readable create() {
			return new Readable(this);
		}
	}

	/**
	 * Constructor.
	 * @param descriptor Readable descriptor
	 */
	protected Readable(Descriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}
}
