package org.sarge.textrpg.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Event;

public class EntityManagerTest {
	private EntityManager manager;
	private Event.Queue queue;
	private Notification.Handler handler;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		queue = mock(Event.Queue.class);
		handler = mock(Notification.Handler.class);
		manager = new EntityManager(queue, handler, mock(Consumer.class));
	}

	@Test
	public void constructor() {
		assertEquals(queue, manager.queue());
		assertEquals(handler, manager.handler());
		assertNotNull(manager.induction());
		assertEquals(0L, manager.updated());
	}

	@Test
	public void update() {
		manager.update(1);
		manager.update(2);
		assertEquals(2, manager.updated());
	}

	@Test
	public void updateInvalid() {
		assertThrows(IllegalStateException.class, () -> manager.update(-1));
	}
}
