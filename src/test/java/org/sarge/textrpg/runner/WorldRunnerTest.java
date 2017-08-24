package org.sarge.textrpg.runner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.Clock;

public class WorldRunnerTest {
	private WorldRunner runner;
	private Clock clock;
	
	@Before
	public void before() {
		clock = mock(Clock.class);
		runner = new WorldRunner(clock);
	}
	
	@After
	public void after() {
		if(runner.isRunning()) {
			runner.stop();
		}
	}
	
	@Test
	public void constructor() {
		assertEquals(false, runner.isRunning());
	}
	
	@Test
	public void start() {
		runner.start();
		assertEquals(true, runner.isRunning());
		verify(clock).update(anyLong());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void startAlreadyStarted() {
		runner.start();
		runner.start();
	}
	
	@Test
	public void stop() {
		runner.start();
		runner.stop();
		assertEquals(false, runner.isRunning());
	}

	@Test(expected = IllegalArgumentException.class)
	public void stopNotStarted() {
		runner.stop();
	}
}
