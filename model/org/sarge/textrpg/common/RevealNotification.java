package org.sarge.textrpg.common;

import org.sarge.lib.util.Check;

/**
 * Notification of a revealed object.
 * @author Sarge
 */
public class RevealNotification extends AbstractNotification {
	private final Hidden hidden;
	private final Description desc;

	/**
	 * Constructor for a revealed object.
	 * @param key			Message key
	 * @param hidden		Revealed object
	 */
	public RevealNotification(String key, Hidden hidden) {
		Check.notEmpty(key);
		Check.notNull(hidden);
		this.hidden = hidden;
		this.desc = new Description(key);
	}
	
	/**
	 * @return Revealed object/entity
	 */
	public Hidden getHidden() {
		return hidden;
	}
	
	@Override
	public Description describe() {
		return desc;
	}
	
	@Override
	public void accept(Handler handler) {
		handler.handle(this);
	}
}
