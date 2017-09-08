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
		when(actor.alignment()).thenReturn(Alignment.GOOD);
		when(target.alignment()).thenReturn(Alignment.EVIL);
		assertEquals(true, ActionHelper.isValidTarget(actor, target));

		// Check can attack neutral target
		when(target.alignment()).thenReturn(Alignment.EVIL);
		assertEquals(true, ActionHelper.isValidTarget(actor, target));

		// Check cannot attach same alignment
		when(target.alignment()).thenReturn(Alignment.GOOD);
		assertEquals(false, ActionHelper.isValidTarget(actor, target));
	}

	@Test
	public void kill() throws ActionException {
		// Add inventory
		final WorldObject obj = new WorldObject(new ObjectDescriptor("object"));
		obj.setParent(actor);

		// Init entity
		when(actor.race()).thenReturn(new Race.Builder("race").build());
		when(actor.location()).thenReturn(loc);

		// Kill
		ActionHelper.kill(actor);

		// Check corpse created and added to location
		assertEquals(1, loc.contents().stream().count());
		final Thing t = loc.contents().stream().iterator().next();
		assertEquals(true, t instanceof Corpse);

		// Check corpse contents
		final Corpse corpse = (Corpse) t;
		assertNotNull(corpse.contents());
		assertEquals(1, corpse.contents().size());
		assertEquals(obj, corpse.contents().stream().iterator().next());
	}

	@Test
	public void registerOpenableEvent() throws ActionException {
		final WorldObject portal = new Portal(new Portal.Descriptor(new ObjectDescriptor.Builder("door").reset(1).build(), Openable.UNLOCKABLE), loc);
		portal.openableModel().get().apply(Openable.Operation.OPEN);
		ActionHelper.registerOpenableEvent(loc, loc, portal, "key");
		assertEquals(1, ActionHelper.QUEUE.size());
		ActionHelper.QUEUE.execute(1);
		// TODO
		//verify(loc, times(2)).broadcast(Actor.SYSTEM, new Message("key", portal));
		assertEquals(false, portal.openableModel().get().isOpen());
		assertEquals(0, ActionHelper.QUEUE.size());
	}

	@Test
	public void findTopicUnknown() {
		assertEquals(Optional.empty(), ActionHelper.findTopic(loc, "cobblers"));
	}

	@Test
	public void findTopic() {
		// Add a topic to this location
		final Entity entity = mock(Entity.class);
		final Topic topic = new Topic("name", Script.NONE);
		when(entity.topics()).thenReturn(Stream.of(topic));
		loc.contents().add(entity);

		// Lookup topic
		assertEquals(Optional.of(topic), ActionHelper.findTopic(loc, "name"));
		verify(entity).topics();

		// Lookup again and check was cached
		assertEquals(Optional.of(topic), ActionHelper.findTopic(loc, "name"));
		verifyNoMoreInteractions(entity);
	}
}
