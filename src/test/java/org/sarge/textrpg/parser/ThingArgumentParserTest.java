package org.sarge.textrpg.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.contents.LimitedContents.LimitsMap;
import org.sarge.textrpg.contents.Thing;
import org.sarge.textrpg.object.Container;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.WordCursor;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.ExitMap;
import org.sarge.textrpg.world.LightLevelProvider;
import org.sarge.textrpg.world.Link;

public class ThingArgumentParserTest extends ActionTestBase {
	private LightLevelProvider light;
	private Thing arg;
	private ThingArgumentParser parser;

	@BeforeEach
	public void before() {
		light = mock(LightLevelProvider.class);
		parser = new ThingArgumentParser(actor, light);
		arg = ObjectDescriptor.of("arg").create();
		when(actor.perceives(arg)).thenReturn(true);
	}

	private Object run() {
		final NameStore store = mock(NameStore.class);
		final WordCursor cursor = new WordCursor("arg", store, Set.of());
		when(store.matches("arg", "arg")).thenReturn(true);
		return parser.parse(cursor);
	}

	@Test
	public void inventory() {
		arg.parent(actor);
		assertEquals(arg, run());
	}

	@Test
	public void location() {
		when(light.isAvailable(loc)).thenReturn(true);
		arg.parent(loc);
		assertEquals(arg, run());
	}

	@Test
	public void locationRemoteObjectsNotVisible() {
		arg.parent(loc);
		run();
		assertEquals(null, run());
	}

	@Test
	public void controllers() {
		final Link link = new Link() {
			@Override
			public Optional<Thing> controller() {
				return Optional.of(arg);
			}
		};
		final Exit exit = Exit.of(Direction.EAST, link, loc);
		when(loc.exits()).thenReturn(ExitMap.of(exit));
		assertEquals(arg, run());
	}

	@Test
	public void container() {
		final Container container = new Container.Descriptor(ObjectDescriptor.of("container"), "in", LimitsMap.EMPTY).create();
		arg.parent(container);
		container.parent(loc);
		when(light.isAvailable(loc)).thenReturn(true);
		when(actor.perceives(container)).thenReturn(true);
		assertEquals(arg, run());
	}
}
