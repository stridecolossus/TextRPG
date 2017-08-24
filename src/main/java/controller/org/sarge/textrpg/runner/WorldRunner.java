package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.object.ToString;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.common.Clock;
import org.sarge.textrpg.common.EventQueue;

/**
 * World runner.
 * @author Sarge
 */
public class WorldRunner {
	private final Clock clock;
	
	private Thread thread;

	/**
	 * Constructor.
	 * @param clock Game-clock
	 */
	public WorldRunner(Clock clock) {
		this.clock = notNull(clock);
	}

	/**
	 * @return Whether this runner is active
	 */
	public boolean isRunning() {
		return thread != null;
	}

	/**
	 * Starts this runner.
	 */
	public void start() {
		if(thread != null) throw new IllegalArgumentException("Already started");
		thread = new Thread(this::run);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Gracefully stops running.
	 */
	public void stop() {
		if(thread == null) throw new IllegalArgumentException("Not running");
		thread = null;
	}

	/**
	 * Main loop.
	 */
	private void run() {
		while(isRunning()) {
			// Advance time
			final long start = System.currentTimeMillis();
			EventQueue.update(start);
			clock.update(start);

			// Sleep for remainder of frame
			final long duration = 50 - (System.currentTimeMillis() - start);
			if(duration > 0) {
				Util.kip(duration);
			}
		}
	}

	@Override
	public String toString() {
		final ToString ts = new ToString(this);
		ts.append("running", isRunning());
		return ts.toString();
	}
}
