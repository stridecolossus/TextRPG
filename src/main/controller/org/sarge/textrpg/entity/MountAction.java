package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.EnumAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity.FollowModel;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;

/**
 * Action to manage a {@link Mount}.
 * @author Sarge
 */
@EnumAction(MountAction.Operation.class)
@RequiresActor
public class MountAction extends AbstractAction {
	/**
	 * Mount action.
	 */
	public enum Operation {
		/**
		 * Leads a mount.
		 */
		LEAD {
			@Override
			protected Response execute(CharacterEntity actor, Mount mount) throws ActionException {
				final FollowModel follower = mount.follower();
				if(follower.isFollowing(actor)) throw ActionException.of("lead.already.following");
				// TODO - mount that doesn't want to follow, e.g. different alignment
				mount.follower().follow(actor);
				return response(this, mount);
			}
		},

		/**
		 * Abandons a mount.
		 */
		ABANDON {
			@Override
			protected Response execute(CharacterEntity actor, Mount mount) throws ActionException {
				final FollowModel follower = mount.follower();
				if(!follower.isFollowing(actor)) throw ActionException.of("abandon.not.following");
				if(mount(actor) == mount) throw ActionException.of("abandon.invalid.mounted");
				follower.stop();
				return response(this, mount);
			}
		},

		/**
		 * Mounts the given mount.
		 */
		MOUNT {
			@Override
			protected Response execute(CharacterEntity actor, Mount mount) throws ActionException {
				if(!mount.follower().isFollowing(actor)) throw ActionException.of("mount.not.leading");
				if(actor.model().stance() == Stance.MOUNTED) throw ActionException.of("mount.already.mounted");
				if(mount.size().isLessThan(actor.size())) throw ActionException.of("mount.too.small");
				actor.model().stance(Stance.MOUNTED);
				actor.movement(new MountMovementMode(actor, mount));
				return response(this, mount);
			}
		},

		/**
		 * Dismounts.
		 */
		DISMOUNT {
			@Override
			protected Response execute(CharacterEntity actor, Mount mount) throws ActionException {
				if(actor.model().stance() != Stance.MOUNTED) throw ActionException.of("dismount.not.mounted");
				actor.model().stance(Stance.DEFAULT);
				actor.movement(null);
				return response(this, mount);
			}
		};

		/**
		 * Performs a mount operation.
		 * @param actor Actor
		 * @param mount Mount
		 * @return Response
		 * @throws ActionException if this operation cannot be performed by the given actor/mount
		 */
		protected abstract Response execute(CharacterEntity actor, Mount mount) throws ActionException;

		/**
		 * Builds a mount operation response.
		 */
		private static Response response(Operation op, Mount mount) {
			return Response.of(new Description(TextHelper.join("action.mount", op.name()), mount.name()));
		}
	}

	/**
	 * Constructor.
	 */
	public MountAction() {
		super(Flag.LIGHT, Flag.REVEALS, Flag.BROADCAST);
	}

	@Override
	public boolean isValid(Stance stance) {
		return stance != Stance.RESTING;
	}

	/**
	 * Helper - Determines the current mount.
	 * @param actor Actor
	 * @return Current mount or <tt>null</tt> if none
	 */
	private static Mount mount(Entity actor) {
		final var mode = actor.movement();
		if(mode instanceof MountMovementMode) {
			final var mounted = (MountMovementMode) mode;
			return mounted.mover();
		}
		else {
			return null;
		}
	}

	/**
	 * Performs a mount operation (on an implicit mount).
	 * @param actor		Actor
	 * @param op		Operation
	 * @return Response
	 * @throws ActionException if the operation cannot be performed on the given mount or no mounts are following the actor
	 * @see Entity#followers()
	 */
	public Response execute(CharacterEntity actor, MountAction.Operation op) throws ActionException {
		switch(op) {
		case LEAD:
			throw ActionException.of("lead.requires.mount");

		case DISMOUNT:
			return Operation.DISMOUNT.execute(actor, mount(actor));

		default:
			// Enumerate current mounts
			final FollowModel follower = actor.follower();
			final List<Mount> mounts = StreamUtil.select(Mount.class, follower.followers()).collect(toList());
			if(mounts.isEmpty()) throw ActionException.of(op.name(), "requires.mount");

			if(op == Operation.ABANDON) {
				// Abandon all mounts
				if(mount(actor) != null) throw ActionException.of("abandon.invalid.mounted");
				mounts.stream().map(Entity::follower).forEach(FollowModel::clear);
				return Response.of("abandon.mount.all");
			}
			else {
				// Otherwise delegate
				return execute(actor, op, mounts.get(0));
			}
		}
	}

	/**
	 * Performs a mount operation.
	 * @param actor		Actor
	 * @param mount		Mount
	 * @return Response
	 * @throws ActionException if the operation cannot be performed on the given mount
	 */
	public Response execute(CharacterEntity actor, MountAction.Operation op, Mount mount) throws ActionException {
		return op.execute(actor, mount);
	}
}
