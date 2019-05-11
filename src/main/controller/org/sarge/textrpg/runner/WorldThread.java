package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.Runner;
import org.sarge.textrpg.util.ServiceComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * World event loop.
 * @author Sarge
 */
@Component
public class WorldThread extends Runner implements ServiceComponent {
	private static final Logger LOG = LoggerFactory.getLogger(WorldThread.class);

	private final Event.Queue.Manager manager;

	private long inc;
	private long frame = 50;

	/**
	 * Constructor.
	 * @param manager Queue manager
	 */
	public WorldThread(Event.Queue.Manager manager) {
		this.manager = notNull(manager);
		setFrameScale(5);
	}

	/**
	 * Sets the duration of a <i>frame</i>.
	 * @param frame Frame duration (ms)
	 */
	@Autowired
	public void setFrameDuration(@Value("${frame.duration}") long frame) {
		this.frame = oneOrMore(frame);
	}

	/**
	 * Sets the game-time scale.
	 * @param scale Scale
	 */
	@Autowired
	public void setFrameScale(@Value("${frame.scale}") int scale) {
		Check.oneOrMore(scale);
		this.inc = 1000 / frame * scale;
	}

	@Override
	public void start() {
		LOG.info("Starting world thread...");
		super.start();
	}

	@Override
	public void stop() {
		LOG.info("Stopping world thread...");
		super.stop();
	}

	@Override
	protected void execute() {
		// Advance clock
		final long start = System.currentTimeMillis();
		manager.advance(inc);

		// Sleep for remainder of frame
		final long duration = System.currentTimeMillis() - start;
		if(duration < frame) {
			Util.kip(frame);
		}
	}
}
