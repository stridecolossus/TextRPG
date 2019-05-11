package org.sarge.textrpg.entity;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.object.WordTrigger;
import org.sarge.textrpg.parser.LiteralArgumentParser;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.ArgumentFormatter;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.world.FacilityRegistry;
import org.sarge.textrpg.world.Terrain;
import org.springframework.stereotype.Component;

/**
 * Action to say something.
 * @author Sarge
 * TODO - chat channels: group, faction?, world?, help?, alignment, area/region/world
 */
@Component
@RequiresActor
public class SayAction extends AbstractAction {
	private static final String KEY = "notification.message";

	private final FacilityRegistry registry;

	/**
	 * Constructor.
	 * @param registry Registry for word triggers
	 */
	public SayAction(FacilityRegistry registry) {
		super(Flag.OUTSIDE);
		this.registry = notNull(registry);
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	public boolean isValid(Terrain terrain) {
		return true;
	}

	@Override
	public boolean isValid(Stance stance) {
		return true;
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		final var trigger = registry.find(actor.location(), WordTrigger.class);
		if(trigger.isPresent()) {
			return ArgumentParser.Registry.of(WordTrigger.class, new LiteralArgumentParser<>(trigger.get()));
		}
		else {
			return ArgumentParser.Registry.EMPTY;
		}
	}

	/**
	 * Builds a message notification.
	 * @param actor		Actor
	 * @param str		Spoken words
	 * @return Message
	 */
	private static Description build(Entity actor, String str) {
		return new Description.Builder(KEY)
			.add("actor", actor.name(), ArgumentFormatter.PLAIN)
			.add("message", str, ArgumentFormatter.PLAIN)
			.build();
	}

	/**
	 * Speaks out loud.
	 * @param actor		Actor
	 * @param str		String
	 * @return Response
	 */
	public Response say(Entity actor, String str) {
		final Description message = build(actor, str);
		actor.location().broadcast(actor, message);
		return Response.EMPTY;
	}

	/**
	 * Tells something to the given entity.
	 * @param actor		Actor
	 * @param entity	Entity to speak to
	 * @param str		String
	 * @return Response
	 */
	public Response tell(Entity actor, Entity entity, String str) {
		// TODO - CAN talk to enemies but language might be mangled => add language
		// if(entity.isValidTarget(actor)) throw ActionException.of("tell.invalid.target");
		final Description message = build(actor, str);
		entity.alert(message);
		return Response.EMPTY;
	}

	/**
	 * Speaks to the group.
	 * @param actor		Actor
	 * @param str		String
	 * @return Response
	 * @throws ActionException if the actor is not in a group
	 */
	public Response group(Entity actor, String str) throws ActionException {
		// Check grouped
		final Group group = actor.model().group();
		if(group == Group.NONE) throw ActionException.of("say.not.grouped");

		// Broadcast message to group
		final Description message = build(actor, str);
		Actor.broadcast(actor, message, group.members());

		return Response.EMPTY;
	}

	/**
	 * Speaks a word trigger command.
	 * @param actor		Actor
	 * @param trigger	Trigger
	 * @return Response
	 */
	public Response say(Entity actor, WordTrigger trigger) {
		say(actor, trigger.name());
		trigger.handler().handle(actor, null, true);
		return Response.EMPTY;
	}
}
