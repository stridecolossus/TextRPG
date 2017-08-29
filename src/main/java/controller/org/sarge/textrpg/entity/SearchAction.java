package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.AbstractActiveAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Event;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Notification;
import org.sarge.textrpg.common.RevealNotification;
import org.sarge.textrpg.common.Thing;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.Location;

/**
 * Action to search for hidden objects in the current location.
 * @author Sarge
 */
public class SearchAction extends AbstractActiveAction {
	private static Comparator<Thing> COMPARATOR = Comparator.comparing(Thing::getVisibility);

	private final int mod;
	private final long time;

	/**
	 * Constructor.
	 * @param mod		Perception multiplier
	 * @param time		Duration multiplier
	 */
	public SearchAction(int mod, long time) {
		Check.oneOrMore(mod);
		Check.oneOrMore(time);
		this.mod = mod;
		this.time = time;
	}

	/**
	 * Discovery event.
	 */
	private class DiscoverEvent implements Event {
		private final Thing obj;
		private final Entity actor;

		/**
		 * Constructor.
		 * @param obj		Object/entity discovered
		 * @param actor		Discoverer
		 */
		public DiscoverEvent(Thing obj, Entity actor) {
			this.obj = obj;
			this.actor = actor;
		}

		@Override
		public void execute() {
			final Notification n = new RevealNotification("search.found.hidden", obj);
			actor.getNotificationHandler().handle(n);
		}
	}

	/**
	 * Search location for hidden objects or entities.
	 * @param actor
	 * @throws ActionException
	 */
	public ActionResponse search(Entity actor) throws ActionException {
		// Calculate search score
		final Location loc = actor.getLocation();
		final int per = actor.getAttributes().get(Attribute.PERCEPTION) * mod;
		final Percentile score = new Percentile(per).invert();

		// Enumerate hidden objects that can be found by this actor
		final EventQueue queue = actor.getEventQueue();
		final List<DiscoverEvent> events = loc.getContents().stream()
			.filter(t -> t.getVisibility().isLessThan(Percentile.ONE))
			.filter(t -> (t instanceof WorldObject) || (t instanceof Entity))
			.filter(StreamUtil.not(actor::perceives))
			.filter(t -> score.isLessThan(t.getVisibility()))
			.sorted(COMPARATOR)
			.map(t -> new DiscoverEvent(t, actor))
			.collect(toList());

		// Register events and determine longest duration
		final List<EventQueue.Entry> entries = new ArrayList<>();
		long longest = 0;
		for(final DiscoverEvent e : events) {
			final int diff = e.obj.getVisibility().intValue() - per;
			final long duration = diff * time;
			final EventQueue.Entry entry = queue.add(e, duration);
			entries.add(entry);
			if(duration > longest) longest = duration;
		}

		// TODO - make duration fixed but mod by perception-level, ensure events 'fit' within duration

		// Start searching
		final Induction induction = new Induction() {
			@Override
			public Description complete() throws ActionException {
				return new Description(entries.isEmpty() ? "search.nothing" : "search.finished");
			}

			@Override
			public void interrupt() {
				entries.stream().forEach(EventQueue.Entry::cancel);
			}
		};
		return new ActionResponse("search.start", induction, longest);
	}
}
