package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.function.Predicate;

import org.sarge.textrpg.common.*;
import org.sarge.textrpg.entity.ActionHelper;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.world.Direction;
import org.sarge.textrpg.world.Exit;
import org.sarge.textrpg.world.Link;

/**
 * Action to describe a location, object or entity.
 * @author Sarge
 */
@SuppressWarnings("unused")
public class LookAction extends AbstractAction {
	private final Clock clock;
	
	public LookAction(Clock clock) {
		this.clock = notNull(clock);
	}

	@Override
	public boolean isParentBlockedAction() {
		return false;
	}

	/**
	 * Describes the current location.
	 */
	public ActionResponse look(Entity actor) throws ActionException {
		final Description desc = actor.getLocation().describe(clock.isDaylight(), actor);
		return new ActionResponse(desc);
	}
	
	/**
	 * Describes the given object.
	 */
	public ActionResponse look(Entity actor, WorldObject obj) throws ActionException {
		final Description desc = ActionHelper.describe(actor, obj);
		return new ActionResponse(desc);
	}

	/**
	 * Looks at a decoration.
	 */
	public ActionResponse look(Entity actor, String decoration) throws ActionException {
		return new ActionResponse("examine.decoration");
	}

	/**
	 * List contents of a container.
	 */
	public ActionResponse look(Entity actor, Container c) throws ActionException {
		if(!c.getOpenableModel().map(Openable::isOpen).orElse(true)) throw new ActionException("list.container.closed");
		// TODO - check for transparent container
		final Predicate<Thing> filter = ContentsHelper.filter(actor);
		final List<Description> list = c.getContents().stream().filter(filter).map(t -> (WorldObject) t).map(WorldObject::describeShort).collect(toList());
		return new ActionResponse(Description.create("list.contents.container", list));
	}
	
	/**
	 * Look at an entity.
	 */
	public ActionResponse look(Entity actor, Entity entity) throws ActionException {
		return new ActionResponse(entity.describe());
	}
	
	/**
	 * Look in a direction.
	 */
	public ActionResponse look(Entity actor, Direction dir) throws ActionException {
		final Exit exit = actor.getLocation().getExits().get(dir);
		if((exit == null) || !exit.perceivedBy(actor)) throw new ActionException("look.direction.invalid");
		final Link link = exit.getLink();
		final Description.Builder builder = link.describe();
		builder.wrap("dir", dir);
		builder.wrap("dest", exit.getDestination().getName());
		final Size size = link.getSize();
		if(size != Size.NONE) {
			builder.wrap("size", "size." + link.getSize());
		}
		return new ActionResponse(builder.build());
	}
}
