package org.sarge.textrpg.runner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.textrpg.util.Event;

public class WorldThreadTest {
	private WorldThread thread;
	private Event.Queue.Manager manager;

	@BeforeEach
	public void before() {
		manager = mock(Event.Queue.Manager.class);
		thread = new WorldThread(manager);
		thread.setFrameDuration(50);
		thread.setFrameScale(5);
	}

	@Test
	public void execute() {
		thread.execute();
		verify(manager).advance(1000 / 50 * 5);
	}
}
