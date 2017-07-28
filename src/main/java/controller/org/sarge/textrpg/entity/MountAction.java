package org.sarge.textrpg.entity;

import java.util.stream.Stream;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.StreamUtil;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;

/**
 * Action to control a mount.
 * @author Sarge
 */
public class MountAction extends AbstractAction {
	/**
	 * Mount actions.
	 */
	public enum Operation {
		/**
		 * Lead the given mount.
		 */
		LEAD {
			@Override
			protected Entity execute(CharacterEntity actor, Entity mount) throws ActionException {
				if(mount == null) throw new ActionException("lead.requires.mount");
				if(mount.isFollowing(actor)) throw new ActionException("lead.already.following");
				mount.follow(actor);
				return mount;
			}
		},

		/**
		 * Ride a mount.
		 */
		MOUNT {
			@Override
			protected Entity execute(CharacterEntity actor, Entity mount) throws ActionException {
				if(mount == null) {
					final Entity m = findMount(actor);
					actor.setMount(m);
					return m;
				}
				else {
					actor.setMount(mount);
					return mount;
				}
			}
		},

		/**
		 * Stop riding.
		 */
		DISMOUNT {
			@Override
			protected Entity execute(CharacterEntity actor, Entity mount) throws ActionException {
				if(mount != null) throw new ActionException("dismount.superfluous.argument");
				actor.setMount(null);
				return mount;
			}
		},

		/**
		 * Abandon a mount.
		 */
		ABANDON {
			@Override
			protected Entity execute(CharacterEntity actor, Entity mount) throws ActionException {
				if(mount == null) {
					final Entity m = findMount(actor);
					abandon(m, actor);
					return m;
				}
				else {
					if(!mount.isFollowing(actor)) throw new ActionException("abandon.not.following");
					abandon(mount, actor);
					return mount;
				}
			}

			private void abandon(Entity mount, CharacterEntity actor) throws ActionException {
				if(actor.getMount().map(m -> m == mount).orElse(false)) throw new ActionException("abandon.riding.mount");
				mount.follow(null);
			}
		};

		/**
		 * Performs this mount action.
		 * @param actor		Actor
		 * @param mount		Mount
		 * @return Mount
		 * @throws ActionException if the operation cannot be performed
		 */
		protected abstract Entity execute(CharacterEntity actor, Entity mount) throws ActionException;

		/**
		 * Helper - Finds exactly <b>one</b> mount following the given entity.
		 */
		protected Entity findMount(Entity actor) throws ActionException {
			final Stream<Entity> mounts = actor.getFollowers().filter(e -> e.getRace().getAttributes().isMount());
			return StreamUtil.findOnly(mounts).orElseThrow(() -> new ActionException("mount.no.mounts", this));
		}
	}

	private final Operation op;
	private final Skill riding;

	/**
	 * Constructor.
	 * @param op		Mount operation
	 * @param riding	Riding skill
	 */
	public MountAction(Operation op, Skill riding) {
		super(op.name());
		Check.notNull(riding);
		this.op = op;
		this.riding = riding;
	}

	@Override
	public boolean isCombatBlockedAction() {
		switch(op) {
		case LEAD:
		case ABANDON:
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean isVisibleAction() {
		switch(op) {
		case MOUNT:
		case DISMOUNT:
			return true;

		default:
			return false;
		}
	}

	/**
	 * Mount action.
	 * @param ctx
	 * @param actor
	 * @throws ActionException
	 */
	@Override
	public ActionResponse execute(ActionContext ctx, Entity actor) throws ActionException {
		return execute(ctx, actor, (Entity) null);
	}

	/**
	 * Mount action.
	 * @param ctx
	 * @param actor
	 * @param entity
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, Entity entity) throws ActionException {
		// Check can ride and argument is a mount
		if((entity != null) && !entity.getRace().getAttributes().isMount()) throw new ActionException("mount.not.mount", op);
		getSkillLevel(actor, riding);

		// Perform action
		final Entity mount = op.execute((CharacterEntity) actor, entity);

		// Build response
		return response(mount);
	}
}
