package org.sarge.textrpg.runner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sarge.textrpg.common.ActionContext;

public class WorldRunnerTest {
	private WorldRunner runner;
	
	@Before
	public void before() {
		runner = new WorldRunner(mock(ActionContext.class));
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
