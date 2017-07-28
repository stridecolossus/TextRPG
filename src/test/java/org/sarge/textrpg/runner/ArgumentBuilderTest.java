package org.sarge.textrpg.runner;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.world.Direction;

public class ArgumentBuilderTest {
	private Actor actor;
	
	@Before
	public void before() {
		actor = mock(Actor.class);
	}
	
	@Test
	public void enumeration() {
		final ArgumentBuilder builder = ArgumentBuilder.enumeration(Direction.class);
		assertArrayEquals(Arrays.stream(Direction.class.getEnumConstants()).toArray(), builder.stream(actor).toArray());
	}
	
	@Test
	public void string() {
		assertArrayEquals(new String[]{"str"}, ArgumentBuilder.object("str").stream(actor).toArray());
	}
	
	@Test
	public void contents() {
		final Contents contents = new Contents();
		final Thing one = mock(Thing.class);
		final Thing two = mock(Thing.class);
		contents.add(one);
		contents.add(two);
		when(actor.perceives(one)).thenReturn(true);
		assertArrayEquals(new Thing[]{one}, ArgumentBuilder.of(contents).stream(actor).toArray());
	}
}
