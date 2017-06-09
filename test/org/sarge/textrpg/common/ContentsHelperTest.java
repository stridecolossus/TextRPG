package org.sarge.textrpg.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.Liquid;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptor.Builder;
import org.sarge.textrpg.object.Receptacle;
import org.sarge.textrpg.object.Receptacle.Descriptor;
import org.sarge.textrpg.object.WorldObject;

public class ContentsHelperTest {
	private WorldObject obj;
	
	@Before
	public void before() {
		obj = mock(WorldObject.class);
	}
	
	@Test
	public void filter() {
		// Create an object
		final Actor actor = mock(Actor.class);
		when(actor.perceives(obj)).thenReturn(true);
		
		// Check filter
		final Predicate<Thing> filter = ContentsHelper.filter(actor);
		assertNotNull(filter);
		assertEquals(true, filter.test(obj));
		assertEquals(false, filter.test(mock(WorldObject.class)));
		assertEquals(false, filter.test(mock(Entity.class)));
	}
	
	@Test
	public void objectMatcher() {
		final ObjectDescriptor descriptor = new ObjectDescriptor("object");
		final Predicate<WorldObject> matcher = ContentsHelper.objectMatcher(descriptor);
		assertEquals(true, matcher.test(new WorldObject(descriptor)));
		assertEquals(false, matcher.test(new WorldObject(new ObjectDescriptor("other"))));
	}
	
	@Test
	public void receptacleMatcher() {
		final Predicate<WorldObject> matcher = ContentsHelper.receptacleMatcher(Liquid.WATER);
		assertEquals(true, matcher.test(Receptacle.WATER));
		assertEquals(false, matcher.test(new Receptacle(new Descriptor(new ObjectDescriptor("oil"), Liquid.OIL, 42, false))));
		assertEquals(false, matcher.test(new WorldObject(new ObjectDescriptor("object"))));
	}

	@Test
	public void categoryMatcher() {
		final Predicate<WorldObject> matcher = ContentsHelper.categoryMatcher("cat");
		assertEquals(true, matcher.test(new WorldObject(new Builder("matches").category("cat").build())));
		assertEquals(false, matcher.test(new WorldObject(new ObjectDescriptor("other"))));
	}
	
	@Test
	public void classFilter() {
		final Receptacle rec = mock(Receptacle.class);
		final Contents contents = new Contents();
		contents.add(obj);
		contents.add(rec);
		final Stream<Receptacle> stream = ContentsHelper.select(contents.stream(), Receptacle.class);
		assertNotNull(stream);
		assertArrayEquals(new Receptacle[]{rec}, stream.toArray());
	}
}
