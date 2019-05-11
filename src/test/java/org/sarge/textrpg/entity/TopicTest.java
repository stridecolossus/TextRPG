package org.sarge.textrpg.entity;

import org.junit.jupiter.api.BeforeEach;

public class TopicTest {
	private Topic topic;

	@BeforeEach
	public void before() {
		topic = new Topic("topic");
	}

//	@Test
//	public void factory() {
//		// Create topics
//		final var parent = TestHelper.parent();
//		final var entity = mock(Entity.class);
//		when(entity.topics()).thenReturn(Set.of(topic));
//		parent.contents().add(entity);
//
//		// Add actor
//		final var actor = mock(Entity.class);
//		parent.contents().add(actor);
//
//		// Check no topics if not perceived
//		final var factory = Topic.factory(actor, parent);
//		assertNotNull(factory);
//		assertNotNull(factory.stream());
//		assertEquals(0, factory.stream().count());
//
//		// Check topic can be found
//		when(actor.perceives(entity)).thenReturn(true);
//		when(entity.isAlive()).thenReturn(true);
//		assertEquals(1, factory.stream().count());
//		assertEquals(topic, factory.stream().iterator().next());
//	}
}
