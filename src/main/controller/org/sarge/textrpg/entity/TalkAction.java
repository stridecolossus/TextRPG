package org.sarge.textrpg.entity;

import java.util.stream.Stream;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.parser.DefaultArgumentParser;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Action to talk to an entity.
 * @author Sarge
 */
@Component
@RequiresActor
public class TalkAction extends AbstractAction {
	/**
	 * Constructor.
	 */
	public TalkAction() {
		super(Flag.OUTSIDE);
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		final ArgumentParser<?> parser = new DefaultArgumentParser<>(ignore -> topics(actor), actor);
		return ArgumentParser.Registry.of(Topic.class, parser);
	}

	/**
	 * Finds all available discussion topics in the actors location.
	 * @param actor Actor
	 * @return Topics
	 */
	private static Stream<CommandArgument> topics(Entity actor) {
		return actor.location().contents()
			.select(CharacterEntity.class)
			.map(Entity::descriptor)
			.flatMap(EntityDescriptor::topics);
	}

	/**
	 * Talks to the given entity.
	 * @param actor			Actor
	 * @param entity		Entity to talk to
	 * @return Entity response
	 * @see Entity#vocation()
	 *
	 * TODO - @AutoEntity to find single entity in current location?
	 *
	 */
	public Response talk(Entity actor, Entity entity) {
		final String vocation = entity.descriptor().race().gear().vocation().orElse("talk.invalid.entity");
		// TODO - check
		// TODO - list general topics / quests
		return Response.of(vocation);
	}

	/**
	 * Discusses the given topic.
	 * @param actor			Actor
	 * @param topic			Discussion topic
	 * @return Response
	 * @throws ActionException TODO
	 */
	public Response discuss(Entity actor, Topic topic) throws ActionException {
//		// TODO - check
//		final Topic topic = entity.topics().filter(t -> t.name().equals(name)).findAny().orElseThrow(() -> ActionException.of("talk.unknown.topic"));
//		// TODO - execute script, if NONE then just return topic name
		return null;
	}
}
