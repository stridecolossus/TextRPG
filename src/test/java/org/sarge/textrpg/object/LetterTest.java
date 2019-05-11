package org.sarge.textrpg.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.TestHelper;

public class LetterTest {
	private Letter letter;

	@BeforeEach
	public void before() {
		letter = new Letter("address", "text", Skill.NONE);
	}

	@Test
	public void constructor() {
		assertEquals(false, letter.isOpen());
	}

	@Test
	public void open() throws ActionException {
		letter.open();
		assertEquals(true, letter.isOpen());
	}

	@Test
	public void openAlreadyOpened() throws ActionException {
		letter.open();
		TestHelper.expect("letter.already.opened", () -> letter.open());
	}
}
