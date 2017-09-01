package org.sarge.textrpg.object;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Condition;
import org.sarge.textrpg.entity.Skill;
import org.sarge.textrpg.entity.Skill.Tier;
import org.sarge.textrpg.object.Readable.Chapter;
import org.sarge.textrpg.object.Readable.Descriptor;

public class ReadableTest {
	private Readable readable;
	private Skill lang;
	
	@Before
	public void before() {
		final List<Chapter> chapters = Arrays.asList(new Chapter("1", "one"), new Chapter("2", "two"));
		lang = new Skill("lang", Collections.singletonList(new Tier(Condition.TRUE, 1)));
		readable = new Readable(new Descriptor(new ObjectDescriptor("book"), "lang", chapters));
	}
	
	@Test
	public void constructor() {
		assertEquals("lang", readable.getDescriptor().language());
		assertEquals(2, readable.getDescriptor().size());
	}
	
	@Test
	public void getChapter() {
		assertEquals("2", readable.getDescriptor().chapter(1).title());
		assertEquals("two", readable.getDescriptor().chapter(1).text());
	}
}
