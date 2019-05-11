package org.sarge.textrpg.util;

import org.sarge.lib.util.AbstractObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template implementation for a repeating thread activity.
 * @author Sarge
 */
public abstract class Runner extends AbstractObject {
	private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

	/**
	 * Creates a runner for the given runnable code.
	 * @param runnable Runnable code
	 * @return Runner
	 */
	public static Runner of(Runnable runnable) {
		return new Runner() {
			@Override
			protected void execute() {
				runnable.run();
			}
		};
	}

	private boolean running;

	/**
	 * @return Whether running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Starts running the given repeating code.
	 * @param runnable Repeating code
	 * @throws IllegalArgumentException if already started
	 */
	public synchronized void start() {
		// Check not started
		if(running) throw new IllegalStateException("Already started");
		running = true;

		// Create loop
		final Runnable wrapper = () -> {
			while(running) {
				try {
					execute();
				}
				catch(Exception e) {
					LOG.error("Uncaught exception during execution", e);
				}
			}
		};

		// Start loop
		final Thread thread = new Thread(wrapper);
		thread.start();
	}

	/**
	 * Stops running.
	 * @throws IllegalArgumentException if not started
	 */
	public synchronized void stop() {
		if(!running) throw new IllegalStateException("Not started");
		running = false;
	}

	/**
	 * Executes an iteration of this runner.
	 */
	protected abstract void execute();
}
