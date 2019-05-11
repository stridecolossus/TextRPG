package org.sarge.textrpg.object;

import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.object.Readable.Section;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TestHelper;
import org.sarge.textrpg.world.LightLevelProvider;

public class ReadActionTest extends ActionTestBase {
	private ReadAction action;
	private Readable readable, book;
	private Skill advanced;
	private LightLevelProvider light;

	@BeforeEach
	public void before() {
		// Init language skills
		advanced = new Skill.Builder().name("advanced").build();
		addRequiredSkill();

		// Create action
		light = mock(LightLevelProvider.class);
		action = new ReadAction(advanced, light);
		action.setLowLanguageFormatter(Percentile.of(33));
		action.setHighLanguageFormatter(Percentile.of(66));

		// Create readable objects
		readable = new Readable.Descriptor(ObjectDescriptor.of("readable"), skill).create();
		book = new Readable.Descriptor(ObjectDescriptor.of("book"), true, skill, List.of(new Section("title", "text", false))).create();
	}

	@Test
	public void readReadable() throws ActionException {
		action.read(actor, readable);
	}

	@Test
	public void readReadableUnknownLanguage() throws ActionException {
		final Readable readable = new Readable(new Readable.Descriptor(ObjectDescriptor.of("readable"), Skill.NONE));
		TestHelper.expect("read.unknown.language", () -> action.read(actor, readable));
	}

	@Test
	public void readLetter() throws ActionException {
		final Letter letter = new Letter("address", "text", skill);
		letter.open();
		action.read(actor, letter);
	}

	@Test
	public void readLetterNotOpened() throws ActionException {
		final Letter letter = new Letter("address", "text", skill);
		TestHelper.expect("letter.not.opened", () -> action.read(actor, letter));
	}

	@Test
	public void listBookChapters() throws ActionException {
		action.read(actor, book);
	}

	@Test
	public void readChapter() throws ActionException {
		action.read(actor, book, 1);
	}

	@Test
	public void readChapterInvalidIndex() throws ActionException {
		TestHelper.expect("read.invalid.index", () -> action.read(actor, book, 999));
	}
}
