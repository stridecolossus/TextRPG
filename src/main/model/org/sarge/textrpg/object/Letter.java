package org.sarge.textrpg.object;

import java.util.List;

import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.ActionException;

/**
 * A <i>letter</i> is a readable object that must be opened before it can be read.
 * @author Sarge
 */
public class Letter extends Readable {
	private static final ObjectDescriptor LETTER = ObjectDescriptor.of("letter");

	private boolean open;

	/**
	 * Constructor.
	 * @param address		Address of this letter
	 * @param text			Text
	 * @param lang			Language used to write this letter
	 */
	public Letter(String address, String text, Skill lang) {
		super(new Readable.Descriptor(LETTER, false, lang, List.of(new Section(address, text, false))));
	}

	/**
	 * @return Whether this letter has been opened
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Opens this letter.
	 * @throws ActionException if this letter has already been opened
	 */
	protected void open() throws ActionException {
		if(open) throw ActionException.of("letter.already.opened");
		open = true;
	}
}
