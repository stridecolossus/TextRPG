package org.sarge.textrpg.world;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;
import java.util.Iterator;
import java.util.function.Function;

import org.sarge.textrpg.common.Induction;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.util.ActionException;
import org.springframework.stereotype.Component;

/**
 * Helper for following something.
 * @author Sarge
 */
@Component
public class FollowHelper {
	private final MovementController mover;

	/**
	 * Constructor.
	 * @param mover Movement controller
	 */
	public FollowHelper(MovementController mover) {
		this.mover = notNull(mover);
	}

	/**
	 * Starts a following induction.
	 * @param actor			Actor
	 * @param start			Initial exit
	 * @param mapper		Finds the next exit from the given location
	 * @param message		Finished message suffix
	 * @return Response
	 */
	public Response follow(Entity actor, Exit start, Function<Location, Exit> mapper, String message) {
		// Create iterator
		final Iterator<Exit> itr = new Iterator<>() {
			private Exit next = start;

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public Exit next() {
				final Exit exit = next;
				next = mapper.apply(next.destination());
				return exit;
			}
		};

		// Create induction
		final Induction induction = () -> {
			if(!itr.hasNext()) throw ActionException.of("follow.finished", message);
			mover.move(actor, itr.next(), 1); // TODO - modifier?
			return Response.DISPLAY_LOCATION;
		};

		// Start following
		// TODO - duration ~ stance/mount
		final Duration duration = Duration.ofSeconds(5);
		final Induction.Descriptor descriptor = new Induction.Descriptor.Builder().period(duration).flag(Induction.Flag.REPEATING).build();
		return Response.of(new Induction.Instance(descriptor, induction));
	}
}
