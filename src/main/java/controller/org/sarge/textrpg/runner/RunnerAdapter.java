package org.sarge.textrpg.runner;

import org.sarge.lib.util.Check;

/**
 * Adapter for a infinite processing loop.
 * <p>
 * Assumes that the given underlying runnable is iterative, i.e.
 * <p>
 * Usage:
 * <code>
 *  // Start new infinite processing loop
 *  final Runnable task = () -> {
 *      // Performs one iteration of the process
 *      ...
 *  };
 *  final Runner runner = new Runner(task);
 *  runner.start();
 *
 *  ...
 *
 *  // Kill the runner later on
 *  runner.stop();
 * </code>
 * @author Sarge
 */
public class RunnerAdapter {
    private final Runnable runnable;

    private Thread thread;

    /**
     * Constructor.
     * @param runnable Process
     */
    public RunnerAdapter(Runnable runnable) {
        Check.notNull(runnable);
        this.runnable = runnable;
    }

    /**
     * @return Whether this runner is active
     */
    public boolean isRunning() {
        return (thread != null) && thread.isAlive();
    }

    /**
     * Starts this runner.
     * @throws IllegalArgumentException if already running
     */
    public void start() {
        if(isRunning()) throw new IllegalArgumentException("Already running");
        final Runnable wrapper = () -> {
            while(isRunning()) {
                runnable.run();
            }
        };
        thread = new Thread(wrapper);
        thread.setDaemon(false);
        thread.start();
    }

    /**
     * Stops this runner.
     * @throws IllegalArgumentException if not running
     */
    public void stop() {
        if(!isRunning()) throw new IllegalArgumentException("Not running");
        thread = null;
    }
}
