package org.sarge.textrpg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RunnerTest {
	private Runner runner;
	private Semaphore semaphore;

	@BeforeEach
	public void before() {
		semaphore = new Semaphore(0);
		runner = Runner.of(semaphore::release);
	}

	@AfterEach
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
		assertTimeout(Duration.ofSeconds(5), () -> {
			try {
				semaphore.acquire();
			}
			catch(InterruptedException e) {
				fail("Failed to acquire semaphore");
			}
		});
	}

	@Test
	public void startAlreadyRunning() {
		runner.start();
		assertThrows(IllegalStateException.class, () -> runner.start());
	}

	@Test
	public void startUncaughtException() {
		final Runnable runnable = () -> {
			throw new RuntimeException();
		};
		runner = Runner.of(runnable);
		runner.start();
	}

	@Test
	public void stop() {
		runner.start();
		runner.stop();
		assertEquals(false, runner.isRunning());
	}

	@Test
	public void stopNotRunning() {
		assertThrows(IllegalStateException.class, () -> runner.stop());
	}
}
