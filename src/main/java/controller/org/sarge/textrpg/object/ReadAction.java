package org.sarge.textrpg.object;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Readable.Chapter;
import org.sarge.textrpg.object.Readable.Descriptor;

/**
 * Action to read something.
 * @author Sarge
 * @see Readable
 */
public class ReadAction extends AbstractAction {
	@Override
	public boolean isVisibleAction() {
		return true;
	}
	
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}
	
	/**
	 * Reads something with a single chapter.
	 * @param actor
	 * @param readable
	 * @throws ActionException
	 */
	public ActionResponse read(Entity actor, Readable readable) throws ActionException {
		final Descriptor descriptor = readable.getDescriptor();
		if(descriptor.size() == 1) {
			return readChapter(descriptor.chapter(0), descriptor.language(), actor);
		}
		else {
			throw new ActionException("read.requires.index");
		}
	}
	
	/**
	 * Read specified chapter.
	 * @param actor
	 * @param readable
	 * @param index
	 * @throws ActionException
	 */
	public ActionResponse read(Entity actor, Readable readable, Integer index) throws ActionException {
		final Descriptor descriptor = readable.getDescriptor();
		if((index < 1) || (index >= descriptor.size())) throw new ActionException("read.invalid.chapter");
		return readChapter(descriptor.chapter(index - 1), descriptor.language(), actor);
	}

	/**
	 * Reads a chapter.
	 * @param c			Chapter to read
	 * @param lang		Language skill
	 * @param actor		Actor
	 * @throws ActionException if the actor does not possess the language
	 */
	private static ActionResponse readChapter(Chapter c, String lang, Entity actor) throws ActionException {
		// Check language
		// TODO - just need one point? readable has difficulty?
		//getSkillLevel(actor, lang);
		
		// Read chapter
		final Description description = new Description.Builder("readable")
			.add("title", c.title())
			.add("text", c.text())
			.build();
		return new ActionResponse(description);
	}
}
