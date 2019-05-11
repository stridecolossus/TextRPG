package org.sarge.textrpg.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Event;

public class TransientModelTest {
	private static final Duration DURATION = Duration.ofMinutes(1);

	private TransientModel model;
	private Event.Queue.Manager manager;
	private Object item;

	@BeforeEach
	public void before() {
		manager = new Event.Queue.Manager();
		model = new TransientModel(manager.queue("queue"));
		item = new Object();
	}

	@Test
	public void constructor() {
		assertNotNull(model.stream());
		assertEquals(0, model.stream().count());
	}

	@Test
	public void add() {
		model.add(item, DURATION);
		assertEquals(true, model.contains(item));
		assertArrayEquals(new Object[]{item}, model.stream().toArray());
	}

	@Test
	public void addDuplicate() {
		model.add(item, DURATION);
		assertThrows(IllegalArgumentException.class, () -> model.add(item, DURATION));
	}

	@Test
	public void forget() {
		model.add(item, DURATION);
		manager.advance(DURATION.toMillis());
		assertEquals(false, model.contains(item));
		assertEquals(0, model.stream().count());
	}
}
