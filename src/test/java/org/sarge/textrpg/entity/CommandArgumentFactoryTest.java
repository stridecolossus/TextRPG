package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.common.ActionTestBase;
import org.sarge.textrpg.common.CommandArgument;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.WorldObject;

public class CommandArgumentFactoryTest extends ActionTestBase {
	private WorldObject arg;

	@BeforeEach
	public void before() {
		arg = ObjectDescriptor.of("arg").create();
		when(actor.perceives(arg)).thenReturn(true);
	}

	@Test
	public void literal() {
		final CommandArgumentFactory<?> literal = CommandArgumentFactory.of(arg);
		assertNotNull(literal.stream(actor));
		assertArrayEquals(new CommandArgument[]{arg}, literal.stream(actor).toArray());
	}

	@Test
	public void compound() {
		final CommandArgumentFactory<?> literal = CommandArgumentFactory.of(arg);
		final CommandArgumentFactory<?> compound = CommandArgumentFactory.compound(List.of(literal, literal));
		assertNotNull(literal.stream(actor));
		assertArrayEquals(new CommandArgument[]{arg, arg}, compound.stream(actor).toArray());
	}

	@Test
	public void empty() {
		assertNotNull(CommandArgumentFactory.EMPTY.stream(actor));
		assertEquals(0, CommandArgumentFactory.EMPTY.stream(actor).count());
	}
}
