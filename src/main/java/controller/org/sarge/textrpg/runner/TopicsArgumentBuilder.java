package org.sarge.textrpg.runner;

import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.ContentsHelper;
import org.sarge.textrpg.entity.CharacterEntity;

/**
 * Builder for NPC conversation topics.
 * @author Sarge
 */
public class TopicsArgumentBuilder implements ArgumentBuilder {
	private final Contents contents;
	
	/**
	 * Constructor.
	 * @param contents Location contents
	 */
	public TopicsArgumentBuilder(Contents contents) {
		Check.notNull(contents);
		this.contents = contents;
	}
	
	@Override
	public Stream<?> stream(Actor actor) {
		return ContentsHelper.select(contents.stream(), CharacterEntity.class).flatMap(CharacterEntity::getTopics);
	}
}
