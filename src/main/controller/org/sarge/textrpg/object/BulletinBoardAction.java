package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.sarge.lib.collection.Pair;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.object.Bulletin.Repository;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.world.FacilityRegistry;

/**
 * Bulletin board interactions.
 * @author Sarge
 */
@RequiresActor
public class BulletinBoardAction extends AbstractAction {
	/**
	 * Bulletin-board descriptor.
	 */
	public static final ObjectDescriptor BULLETIN_BOARD = ObjectDescriptor.fixture("bulletin.board");

	private final Bulletin.Repository repository;
	private final FacilityRegistry registry;

	private final Map<Pair<PlayerCharacter, Bulletin.Group>, List<Bulletin>> cache = new WeakHashMap<>();

	/**
	 * Constructor.
	 * @param repository 		Bulletin repository
	 * @param registry			Facility registry for bulletin boards
	 */
	public BulletinBoardAction(Repository repository, FacilityRegistry registry) {
		super(Flag.LIGHT);
		this.repository = notNull(repository);
		this.registry = notNull(registry);
	}

	/**
	 * Lists unread bulletin posts.
	 * @param actor		Actor
	 * @param group		Bulletin group
	 * @return Unread bulletin posts
	 * @throws ActionException if there is no bulletin board in the current location
	 */
	public Response list(PlayerCharacter actor, Bulletin.Group group) throws ActionException {
		check(actor);
		final var posts = posts(actor, group);
		if(posts.isEmpty()) {
			return Response.of("bulletin.list.empty");
		}
		else {
			//return Response.of(posts);
			return null; // TODO
		}
	}

	/**
	 * Reads the next unread bulletin post in the given group.
	 * @param actor		Actor
	 * @param group		Bulletin group
	 * @return Next unread bulletin
	 * @throws ActionException if there is no bulletin board in the current location
	 * @throws ActionException if there is are no more unread posts in the given group
	 */
	public Response read(PlayerCharacter actor, Bulletin.Group group) throws ActionException {
		check(actor);
		return null; // TODO
	}

	public Response read(PlayerCharacter actor, Bulletin.Group group, Integer index) throws ActionException {
		check(actor);
		return null; // TODO
	}

	/**
	 * Retrieves unread bulletin posts for the given actor and group.
	 * @param actor		Actor
	 * @param group		Bulletin group
	 * @return Bulletin posts
	 */
	private List<Bulletin> posts(PlayerCharacter actor, Bulletin.Group group) {
		final Pair<PlayerCharacter, Bulletin.Group> key = Pair.of(actor, group);
		return cache.computeIfAbsent(key, ignore -> repository.posts(actor.name(), group).collect(toList()));
	}

	/**
	 * Checks that a bulletin-board is present in the actors location.
	 */
	private void check(Entity actor) throws ActionException {
		if(!registry.find(actor.location(), WorldObject.class).map(WorldObject::descriptor).map(BULLETIN_BOARD::equals).orElse(false)) {
			throw ActionException.of("bulletin.board.none");
		}
	}
}
