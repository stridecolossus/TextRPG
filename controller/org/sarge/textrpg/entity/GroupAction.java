package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.common.Notification;

/**
 * Action performed on a {@link Group}.
 * @author Sarge
 */
public class GroupAction extends AbstractAction {
	/**
	 * Group operations.
	 */
	public enum Operation {
		ADD {
			@Override
			protected void execute(Group group, Entity entity) throws ActionException {
				group.add(entity);
			}
		},
		
		REMOVE {
			@Override
			protected void execute(Group group, Entity entity) throws ActionException {
				group.remove(entity);
			}
		},
		
		LEADER {
			@Override
			protected void execute(Group group, Entity entity) throws ActionException {
				group.setLeader(entity);
			}
		},
		
		LEAVE {
			@Override
			protected void execute(Group group, Entity entity) throws ActionException {
				group.remove(entity);
			}
		},
		
		DISBAND {
			@Override
			protected void execute(Group group, Entity entity) throws ActionException {
				group.disband();
			}
		},
		
		GROUP {
			@Override
			protected void execute(Group group, Entity entity) throws ActionException {
				// TODO
				System.out.println(group.getMembers().collect(toList()));
			}
		};
		
		/**
		 * Performs this action.
		 * @param group		Group
		 * @param entity	Entity argument
		 * @throws ActionException if the action cannot be performed
		 */
		protected abstract void execute(Group group, Entity entity) throws ActionException;
		
		/**
		 * @return Whether this action requires an entity argument
		 */
		protected boolean hasArgument() {
			switch(this) {
			case ADD:
			case REMOVE:
			case LEADER:
				return true;
				
			default:
				return false;
			}
		}
	}
	
	private final Operation op;
	
	/**
	 * Constructor.
	 * @param op Group operation
	 */
	public GroupAction(Operation op) {
		super("group." + op.name());
		this.op = op;
	}

	@Override
	public boolean isCombatBlockedAction() {
		return false;
	}
	
	@Override
	public boolean isLightRequiredAction() {
		return false;
	}
	
	@Override
	public boolean isParentBlockedAction() {
		return false;
	}
	
	@Override
	public ActionResponse execute(ActionContext ctx, Entity actor) throws ActionException {
		return execute(ctx, actor, (Entity) null);
	}
	
	/**
	 * Group action.
	 * @param ctx
	 * @param actor
	 * @param entity
	 * @throws ActionException
	 */
	public ActionResponse execute(ActionContext ctx, Entity actor, Entity entity) throws ActionException {
		// Validate argument
		if((entity != null) != op.hasArgument()) throw new ActionException("group.requires.argument");

		// Check is leader
		final Group group = actor.getGroup().orElseThrow(() -> new ActionException("group.not.grouped"));
		if(op != Operation.GROUP) {
			if(group.getLeader() != actor) throw new ActionException("group.not.leader");
		}
		
		// Delegate
		if(op == Operation.LEAVE) {
			op.execute(group, actor);
		}
		else {
			op.execute(group, entity);
		}
		
		// Notify group
		if(op != Operation.GROUP) {
			final Notification message;
			final String key = "group." + op;
			if(entity == null) {
				message = new Message(key);
			}
			else {
				final Description desc = new Description.Builder(key).add("name", entity.getName()).build();
				message = desc.toNotification();
			}
			group.getMembers().forEach(e -> e.getNotificationHandler().handle(message));
		}
		
		// Build response
		final Description.Builder res = new Description.Builder("group." + op.name());
		if(entity != null) {
			res.wrap("name", entity);
		}
		return new ActionResponse(res.build());
	}
}
