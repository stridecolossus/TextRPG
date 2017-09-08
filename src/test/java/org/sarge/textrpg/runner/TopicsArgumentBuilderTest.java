package org.sarge.textrpg.runner;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.Test;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.entity.CharacterEntity;

public class TopicsArgumentBuilderTest {
	@Test
	public void stream() {
		final CharacterEntity ch = mock(CharacterEntity.class);
		final Topic topic = new Topic("topic", Script.NONE);
		final Contents contents = new Contents();
		when(ch.topics()).thenReturn(Stream.of(topic));
		contents.add(ch);
		final ArgumentBuilder builder = new TopicsArgumentBuilder(contents);
		assertArrayEquals(new Topic[]{topic}, builder.stream(null).toArray());
	}
}
