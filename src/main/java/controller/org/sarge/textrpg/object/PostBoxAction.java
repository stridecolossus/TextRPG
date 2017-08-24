package org.sarge.textrpg.object;

import static java.util.stream.Collectors.joining;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.object.PostManager.Letter;

/**
 * Action to read and post letters at a post-box.
 * @author Sarge
 */
public class PostBoxAction extends AbstractAction {
	/**
	 * Post-box operation.
	 */
	public enum Operation {
		LIST {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				final String list = mgr.getLetters(actor).map(Object::toString).collect(joining(","));
				return list;
			}
		},
		
		LIST_UNREAD {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				return null;
			}
		},
		
		LIST_SENT {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				return null;
			}
		},
		
		READ {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				return null;
			}
		},
		
		MARK_UNREAD {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				return null;
			}
		},
		
		WRITE {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				return null;
			}
		},
		
		ATTACH {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				return null;
			}
		},
		
		DELETE {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				return null;
			}
		},
		
		DELETE_READ {
			@Override
			protected String execute(Entity actor, Letter letter, PostManager mgr) {
				return null;
			}
		};
		
		/**
		 * Performs this letter operation.
		 * @param actor		Actor
		 * @param letter	Letter
		 * @param mgr		Post-box manager
		 * @return Response
		 */
		protected abstract String execute(Entity actor, Letter letter, PostManager mgr);
		
		/**
		 * 
		 * @return Whether this operation requires an indexed letter
		 */
		private boolean isIndexed() {
			switch(this) {
			case READ:
			case MARK_UNREAD:
			case ATTACH:
			case DELETE:
				return true;
				
			default:
				return false;
			}
		}
	}
	
	private final Operation op;
	private final PostManager mgr;
	
	/**
	 * Constructor.
	 * @param op	Letter operation
	 * @param mgr	Post-box
	 */
	public PostBoxAction(Operation op, PostManager mgr) {
		this.op = notNull(op);
		this.mgr = notNull(mgr);
	}

	@Override
	public ActionResponse execute(Entity actor) throws ActionException {
		// Check available post-box in this location
		checkPostBox(actor);
		
		// Delegate
		final String response = op.execute(actor, null, mgr);
		
		// Generate response
		// TODO
		//actor.notify(new Message("post.box." + op, response == null ? AbstractAction.OK : response));
		return null;
	}

	public void execute(Entity actor, Integer index) throws ActionException {
		final Letter letter = mgr.getLetter(index).orElseThrow(() -> new ActionException(op + ".invalid.index"));
		final String response = op.execute(actor, letter, mgr);
		//actor.notify(new Message("post.box." + op, response == null ? AbstractAction.OK : response));
	}
	
	public void execute(Entity actor, Entity entity, String text) throws ActionException {
		// Check can send letter
		checkPostBox(actor);
		if(!(actor instanceof Player)) throw new ActionException("post.invalid.recipient");
		
		// Send letter
		// TODO - clock
//		final Letter letter = new Letter(actor.getName(), text, null, ctx.getTime()); // TODO - attachments
//		mgr.send(entity, letter);
	}
	
	/**
	 * Checks for a post-box in this location.
	 */
	private static void checkPostBox(Entity actor) throws ActionException {
		// TODO - this is ugly, must be a nicer way? maybe move categories to Thing?
		final boolean valid = actor.getLocation().getContents().stream()
			.filter(t -> t instanceof WorldObject)
			.map(t -> (WorldObject) t)
			.anyMatch(obj -> obj.getDescriptor().getCharacteristics().getCategories().anyMatch(cat -> cat.equals("post.box")));
		if(!valid) throw new ActionException("post.no.postbox");
	}
}
