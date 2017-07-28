package org.sarge.textrpg.runner;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.EventQueue;

/**
 * World runner.
 * @author Sarge
 */
public class WorldRunner {
	private final ActionContext ctx;

	private Thread thread;

	/**
	 * Constructor.
	 * @param ctx Context
	 */
	public WorldRunner(ActionContext ctx) {
		Check.notNull(ctx);
		this.ctx = ctx;
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
		while(thread != null) {
			// Advance time
			final long start = System.currentTimeMillis();
			synchronized(ctx) {
				EventQueue.update(start);
			}

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
