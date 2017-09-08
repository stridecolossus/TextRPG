package org.sarge.textrpg.object;

import java.util.ArrayList;
import java.util.List;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Description;

/**
 * Readable object such as a letter, book or plaque.
 * @author Sarge
 * TODO - openable?
 */
public class Readable extends WorldObject {
	/**
	 * Chapter.
	 */
	public static final class Chapter {
		private final String title;
		private final String text;

		/**
		 * Constructor.
		 * @param title		Chapter title
		 * @param text		Text
		 */
		public Chapter(String title, String text) {
			Check.notEmpty(title);
			Check.notEmpty(text);
			this.title = title;
			this.text = text;
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
		
		@Override
		public String toString() {
			return title;
		}
	}
	
	/**
	 * Descriptor for a readable object.
	 * TODO - language should be a skill?
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final String lang;
		private final List<Chapter> chapters;
		
		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param lang				Language skill
		 * @param chapters			Chapter(s)
		 */
		public Descriptor(ObjectDescriptor descriptor, String lang, List<Chapter> chapters) {
			super(descriptor);
			Check.notNull(lang);
			Check.notEmpty(chapters);
			this.lang = lang;
			this.chapters = new ArrayList<>(chapters);
		}

		/**
		 * @return Language skill
		 */
		public String language() {
			return lang;
		}
		
		/**
		 * @return Number of chapters
		 */
		public int size() {
			return chapters.size();
		}
		
		/**
		 * Looks up a chapter.
		 * @param index Chapter index
		 * @return Chapter text
		 */
		public Chapter chapter(int index) {
			return chapters.get(index);
		}

		@Override
		public WorldObject create() {
			return new Readable(this);
		}
	}
	
	/**
	 * Constructor.
	 * @param descriptor Readable descriptor
	 */
	public Readable(Descriptor descriptor) {
		super(descriptor);
	}
	
	@Override
	public Descriptor descriptor() {
		return (Descriptor) super.descriptor();
	}
	
	@Override
	protected void describe(Description.Builder description) {
		final Descriptor descriptor = descriptor();
		description.add("lang", descriptor.lang);
		description.add("chapters", descriptor.chapters.size());
	}
}
