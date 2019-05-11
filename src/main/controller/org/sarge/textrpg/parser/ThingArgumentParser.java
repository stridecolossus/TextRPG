package org.sarge.textrpg.parser;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.contents.Contents;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.entity.CommandArgumentFactory;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.util.WordCursor;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.LightLevelProvider;

/**
 * Argument parser for objects relative to the actor.
 * @author Sarge
 */
public class ThingArgumentParser extends AbstractEqualsObject implements ArgumentParser<Thing> {
	/**
	 * Adapter for a command-argument factory that returns empty results if there is no light available.
	 */
	private static class LightCommandArgumentFactoryAdapter implements CommandArgumentFactory<Thing> {
		private final CommandArgumentFactory<Thing> factory;
		private final LightLevelProvider light;

		/**
		 * Constructor.
		 * @param factory		Delegate factory
		 * @param light			Light-level
		 */
		public LightCommandArgumentFactoryAdapter(CommandArgumentFactory<Thing> factory, LightLevelProvider light) {
			this.factory = notNull(factory);
			this.light = notNull(light);
		}

		@Override
		public Stream<? extends Thing> stream(Entity actor) {
			if(light.isAvailable(actor.location())) {
				return factory.stream(actor);
			}
			else {
				return Stream.empty();
			}
		}
	}

	/**
	 * Filter for objects that can be used by the given actor.
	 * @param actor Actor
	 * @return Filter
	 */
	private static Predicate<Thing> isValid(Actor actor) {
		return obj -> obj.isAlive() && actor.perceives(obj);
	}

	/**
	 * Tests whether the given container contents are available.
	 * @param contents Contents
	 * @return Whether contents are available
	 */
	private static boolean isValidContents(Contents contents) {
		switch(contents.policy()) {
		case DEFAULT:
		case PERCEIVED:
			return true;

		default:
			return false;
		}
	}

	/**
	 * Factory for the actors inventory.
	 */
	private static final CommandArgumentFactory<Thing> INVENTORY = actor -> actor.contents().stream();

	/**
	 * Factory for the contents of the current location.
	 */
	private static final CommandArgumentFactory<Thing> LOCATION = actor -> actor.location().contents().stream().filter(isValid(actor));

	/**
	 * Factory for the contents of available containers.
	 */
	private static final CommandArgumentFactory<Thing> CONTAINERS = actor -> actor.location().contents()
		.select(Container.class)
		.filter(isValid(actor))
		.map(Container::contents)
		.filter(ThingArgumentParser::isValidContents)
		.flatMap(Contents::stream)
		.filter(isValid(actor));

	/**
	 * Factory for link controllers in the current location.
	 */
	private static final CommandArgumentFactory<Thing> LINK_CONTROLLERS = actor -> actor.location().exits().stream()
		.map(Exit::link)
		.flatMap(link -> link.controller().stream())
		.filter(actor::perceives);

	private final ArgumentParser<? extends Thing> parser;

	/**
	 * Constructor.
	 * @param actor		Actor
	 * @param light		Light-level
	 */
	public ThingArgumentParser(Entity actor, LightLevelProvider light) {
		parser = build(actor, light);
	}

	@Override
	public Thing parse(WordCursor cursor) {
		return parser.parse(cursor);
	}

	/**
	 * Builds the argument parser for the given actor.
	 * @param actor		Actor
	 * @param light		Light-level
	 */
	private static ArgumentParser<? extends Thing> build(Entity actor, LightLevelProvider light) {
		final var location = new LightCommandArgumentFactoryAdapter(LOCATION, light);
		final var containers = new LightCommandArgumentFactoryAdapter(CONTAINERS, light);
		final List<CommandArgumentFactory<? extends Thing>> factories = List.of(INVENTORY, LINK_CONTROLLERS, location, containers);
		return new DefaultArgumentParser<>(CommandArgumentFactory.compound(factories), actor);
	}
}
