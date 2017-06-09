package org.sarge.textrpg.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionTest;
import org.sarge.textrpg.common.DefaultTopic;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Openable;
import org.sarge.textrpg.common.Script;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.Portal;
import org.sarge.textrpg.object.WorldObject;

public class ActionHelperTest extends ActionTest {
	@Test
	public void isValidTarget() {
		// Check can attack opposing alignment
		final Entity actor = mock(Entity.class);
		final Entity target = mock(Entity.class);
		when(actor.getAlignment()).thenReturn(Alignment.GOOD);
		when(target.getAlignment()).thenReturn(Alignment.EVIL);
		assertEquals(true, ActionHelper.isValidTarget(actor, target));

		// Check can attack neutral target
		when(target.getAlignment()).thenReturn(Alignment.EVIL);
		assertEquals(true, ActionHelper.isValidTarget(actor, target));
		
		// Check cannot attach same alignment
		when(target.getAlignment()).thenReturn(Alignment.GOOD);
		assertEquals(false, ActionHelper.isValidTarget(actor, target));
	}
	
	@Test
	public void kill() throws ActionException {
		// Add inventory
		final WorldObject obj = new WorldObject(new ObjectDescriptor("object"));
		obj.setParent(actor);
		
		// Init entity
		when(actor.getRace()).thenReturn(new Race.Builder("race").build());
		when(actor.getLocation()).thenReturn(loc);
		
		// Kill
		ActionHelper.kill(actor);
		
		// Check corpse created and added to location
		assertEquals(1, loc.getContents().stream().count());
		final Thing t = loc.getContents().stream().iterator().next();
		assertEquals(true, t instanceof Corpse);
		
		// Check corpse contents
		final Corpse corpse = (Corpse) t;
		assertNotNull(corpse.getContents());
		assertEquals(1, corpse.getContents().size());
		assertEquals(obj, corpse.getContents().stream().iterator().next());
	}
	
	@Test
	public void registerOpenableEvent() throws ActionException {
		final EventQueue queue = new EventQueue();
		final WorldObject portal = new Portal(new Portal.Descriptor(new ObjectDescriptor.Builder("door").reset(1).build(), Openable.UNLOCKABLE), loc);
		portal.getOpenableModel().get().apply(Openable.Operation.OPEN);
		ActionHelper.registerOpenableEvent(queue, loc, loc, portal, "key");
		assertEquals(1, queue.stream().count());
		queue.update(1);
		// TODO
		//verify(loc, times(2)).broadcast(Actor.SYSTEM, new Message("key", portal));
		assertEquals(false, portal.getOpenableModel().get().isOpen());
		assertEquals(0, queue.stream().count());
	}
	
	@Test
	public void findTopicUnknown() {
		assertEquals(Optional.empty(), ActionHelper.findTopic(loc, "cobblers"));
	}
	
	@Test
	public void findTopic() {
		// Add a topic to this location
		final Entity entity = mock(Entity.class);
		final Topic topic = new DefaultTopic("name", Script.NONE);
		when(entity.getTopics()).thenReturn(Stream.of(topic));
		loc.getContents().add(entity);
		
		// Lookup topic
		assertEquals(Optional.of(topic), ActionHelper.findTopic(loc, "name"));
		verify(entity).getTopics();
		
		// Lookup again and check was cached
		assertEquals(Optional.of(topic), ActionHelper.findTopic(loc, "name"));
		verifyNoMoreInteractions(entity);
	}
}
